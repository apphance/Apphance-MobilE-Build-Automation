package com.apphance.ameba.android.plugins.buildplugin;

import groovy.lang.Closure

import java.io.File

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.android.AndroidArtifactBuilderInfo
import com.apphance.ameba.android.AndroidBuildXmlHelper
import com.apphance.ameba.android.AndroidEnvironment
import com.apphance.ameba.android.AndroidManifestHelper
import com.apphance.ameba.android.AndroidProjectConfiguration
import com.apphance.ameba.android.AndroidProjectConfigurationRetriever
import com.apphance.ameba.android.AndroidSingleVariantBuilder
import com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin;

/**
 * Plugin for various Android related tasks.
 *
 */
class AndroidPlugin implements Plugin<Project> {
    static Logger logger = Logging.getLogger(AndroidPlugin.class)

    ProjectHelper projectHelper
    ProjectConfiguration conf
    AndroidProjectConfigurationRetriever androidConfRetriever
    AndroidProjectConfiguration androidConf
    AndroidManifestHelper manifestHelper
    AndroidSingleVariantBuilder androidBuilder
    AndroidEnvironment androidEnvironment

    def void apply (Project project) {
        use (PropertyCategory) {
            this.projectHelper = new ProjectHelper();
            this.conf = project.getProjectConfiguration()
            this.androidConfRetriever = new AndroidProjectConfigurationRetriever()
            this.androidConf = androidConfRetriever.getAndroidProjectConfiguration(project)
            this.manifestHelper = new AndroidManifestHelper()
            this.androidBuilder = new AndroidSingleVariantBuilder(project, this.androidConf)
            prepareAndroidEnvironment(project)
            prepareJavaEnvironment(project)
            prepareCompileAndroidTask(project)
            prepareUpdateProjectTask(project)
            prepareCleanTask(project)
            prepareCleanClassesTask(project)
            prepareReadAndroidVersionAndProjectNameTask(project)
            prepareReadAndroidProjectConfigurationTask(project)
            prepareAllInstallTasks(project)
            prepareBuildDebugOnlyTask(project)
            prepareBuildReleaseOnlyTask(project)
            prepareAllVariants(project)
            prepareBuildAllTask(project)
            prepareReplacePackageTask(project)
            addAndroidSourceExcludes()
            addAndroidVCSCommits()
        }
    }

    private addAndroidSourceExcludes() {
        conf.sourceExcludes << '**/*.class'
        conf.sourceExcludes << '**/bin/**'
        conf.sourceExcludes << '**/gen/**'
        conf.sourceExcludes << '**/build/*'
        conf.sourceExcludes << '**/local.properties'
    }

    private addAndroidVCSCommits() {
        conf.commitFilesOnVCS << 'AndroidManifest.xml'
    }

    private prepareCompileAndroidTask(Project project) {
        def task = project.task('compileAndroid')
        task.description = "Performs code generation/compile tasks for android (if needed)"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
        task << {
            File gen = new File(project.rootDir,'gen')
            if (!gen.exists() || gen.list().length == 0) {
                logger.lifecycle("Regenerating gen directory by running debug project")
                projectHelper.executeCommand(project, [
                    'ant',
                    'debug'
                ])
            } else {
                logger.lifecycle("Not regenerating gen directory! You might need to run clean in order to get latest data (you can also run any of the android builds)")
            }
        }
        project.javadoc.dependsOn(task)
        project.compileJava.dependsOn(task)
    }

    private void prepareJavaEnvironment(Project project) {
        project.apply plugin: 'java'
        def javaConventions =  project.convention.plugins.java
        javaConventions.sourceSets {
            main {
                output.classesDir = 'build/classes'
                output.resourcesDir = 'build/resources'
                java { srcDir 'src' }
                java { srcDir 'gen' }
            }
            test {
                output.classesDir = 'build/test-classes'
                output.resourcesDir = 'build/test-resources'
                java { srcDir 'test-src' }
            }
        }
        project.compileJava.options.encoding = 'UTF-8'
        project.javadoc.options.encoding = 'UTF-8'
        project.compileTestJava.options.encoding = 'UTF-8'
        project.dependencies.add('compile', project.files('ext-classes'))
        project.dependencies.add('compile', project.files(androidConf.sdkJars))
        project.dependencies.add('compile', project.files(androidConf.libraryJars))
        project.dependencies.add('compile', project.files(androidConf.linkedLibraryJars))
    }

    private void prepareAndroidEnvironment(Project project) {
        logger.lifecycle("Running android update")
        runUpdateRecursively(project, project.rootDir, false, true)
        androidEnvironment = new AndroidEnvironment(project)
        androidConf.sdkDirectory = new File(androidEnvironment.getAndroidProperty('sdk.dir'))
        if (androidConf.sdkDirectory == null) {
            def androidHome = System.getenv("ANDROID_HOME")
            if (androidHome != null) {
                androidConf.sdkDirectory = new File(androidHome)
            }
        }
        if (androidConf.sdkDirectory == null) {
            throw new GradleException('Unable to find location of Android SDK, either\
 set it in local.properties or in ANDROID_HOME environment variable')
        }
        androidConf.excludedBuilds = project.hasProperty('android.excluded.builds') ? project['android.excluded.builds'].split(",")*.trim() : []
        def target = androidEnvironment.getAndroidProperty('target')
        if (target == null) {
            throw new GradleException("target is not defined. Please run 'android update project' or 'android create project' as appropriate")
        }
        androidConf.targetName = target
        if (project.hasProperty('android.minSdk.target') && !project['android.minSdk.target'].empty) {
            androidConf.minSdkTargetName = project['android.minSdk.target']
        } else {
            AndroidManifestHelper helper = new AndroidManifestHelper()
            def targetVersion = helper.readMinSdkVersion(project.rootDir)
            if (targetVersion != null) {
                androidConf.minSdkTargetName = 'android-' + targetVersion
            } else {
                androidConf.minSdkTargetName = androidConf.targetName
            }
        }
        logger.lifecycle("Min SDK target name = " + androidConf.minSdkTargetName)
        if (project.hasProperty('android.test.emulator.target') && !project['android.test.emulator.target'].empty) {
            androidConf.emulatorTargetName = project['android.test.emulator.target']
        } else {
            androidConf.emulatorTargetName = androidConf.targetName
        }
        updateSdkJars()
        updateLibraryProjects(project.rootDir)
    }

    private void updateSdkJars() {
        def target = androidConf.minSdkTargetName
        if (target.startsWith('android')) {
            String version= target.split("-")[1]
            androidConf.sdkJars << new File(androidConf.sdkDirectory,"platforms/android-" + version + "/android.jar")
        } else {
            String version= target.split(':')[2]
            Integer numVersion = version as Integer
            androidConf.sdkJars << new File(androidConf.sdkDirectory,"platforms/android-" + version + "/android.jar")
            if (target.startsWith('Google')) {
                def mapJarFiles = new FileNameFinder().getFileNames(androidConf.sdkDirectory.path,
                        "add-ons/addon*google*apis*google*inc*${version}/libs/maps.jar")
                for (path in mapJarFiles) {
                    androidConf.sdkJars << new File(path)
                }
            }
            if (target.startsWith('KYOCERA Corporation:DTS')) {
                androidConf.sdkJars << new File(androidConf.sdkDirectory,"add-ons/addon_dual_screen_apis_kyocera_corporation_" +
                        version + "/libs/dualscreen.jar")
            }
            if (target.startsWith('LGE:Real3D')) {
                androidConf.sdkJars << new File(androidConf.sdkDirectory,"add-ons/addon_real3d_lge_" +
                        version + "/libs/real3d.jar")
            }
            if (target.startsWith('Sony Ericsson Mobile Communications AB:EDK')) {
                androidConf.sdkJars << new File(androidConf.sdkDirectory,"add-ons/addon_edk_sony_ericsson_mobile_communications_ab_" +
                        version + "/libs/com.sonyericsson.eventstream_1.jar")
            }
        }
        logger.lifecycle("Android SDK jars = " + androidConf.sdkJars)
    }

    private void updateLibraryProjects(File projectDir) {
        File libraryDir = new File(projectDir,"libs")
        if (libraryDir.exists()) {
            libraryDir.eachFile { file ->
                if (file.name.endsWith('.jar')) {
                    androidConf.libraryJars << file
                }
            }
        }
        Properties prop = new Properties()
        prop.load (new FileInputStream(new File(projectDir,'project.properties')))
        prop.each { key, value ->
            if (key.startsWith('android.library.reference.')) {
                File libraryProject = new File(projectDir, value)
                File binProject = new File(libraryProject,'bin')
                if (binProject.exists()) {
                    binProject.eachFile { file ->
                        if (file.name.endsWith('.jar')) {
                            androidConf.linkedLibraryJars << file
                        }
                    }
                }
                updateLibraryProjects(libraryProject)
            }
        }
    }

    private void prepareUpdateProjectTask(Project project) {
        def task = project.task('updateProject')
        task.description = "updates project using android command line"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
        task << {
            runUpdateRecursively(project, project.rootDir, true)
            logger.lifecycle("Performed android update")
        }
    }

    private void runUpdateRecursively(Project project, File currentDir, boolean reRun, boolean silentLogging = false) {
        runUpdateProject(project, currentDir,reRun)
        Properties prop = new Properties()
        prop.load (new FileInputStream(new File(currentDir,'project.properties')))
        prop.each { key, value ->
            if (key.startsWith('android.library.reference.')) {
                File libraryProject = new File(currentDir, value)
                runUpdateRecursively(project, libraryProject, reRun, silentLogging)
            }
        }
    }

    private runUpdateProject(Project project, File directory, boolean reRun, boolean silentLogging = false) {
        if (!new File(directory,'local.properties').exists() || reRun) {
            if (!directory.exists()) {
                throw new GradleException('The directory ${directory} to execute the command, does not exist! Your configuration is wrong.')
            }
            try {
                projectHelper.executeCommand(project, directory, [
                    'android',
                    'update',
                    'project',
                    '-p',
                    '.',
                    '-s'
                ], false, null, null, 1, silentLogging)
            } catch (IOException e) {
                throw new GradleException("""The android utility is probably not in your PATH. Please add it!
    BEWARE! For eclipse junit build it's best to add symbolic link
    to your \$ANDROID_HOME/tools/android in /usr/bin""", e)
            }
        }
    }

    private void prepareCleanTask(Project project) {
        def task = project.task('cleanAndroid')
        task.description = "cleans the application"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
        task << {
            projectHelper.executeCommand(project,['ant', 'clean'])
            File tmpDir = new File(project.rootDir,"tmp")
            project.ant.delete(dir: tmpDir)
        }
        project.clean.dependsOn(task)
        task.dependsOn(project.cleanConfiguration)
    }


    private void prepareCleanClassesTask(Project project) {
        def task = project.task('cleanClasses')
        task.description = "Cleans only the compiled classes (removes instrumentation)"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
        task << {
            project.ant.delete(dir: new File(project.rootDir,"build"))
        }
    }

    private prepareInstallTask(Project project, String variant) {
        String debugRelease  = androidBuilder.getDebugRelease(project, variant)
        def testTask = project.task("install${debugRelease}-${variant}")
        testTask.description = "Installs " + variant
        testTask.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
        def firstLetterLowerCase = debugRelease[0].toLowerCase()
        testTask << {
            File apkFile = new File(conf.targetDirectory,"${conf.projectName}-${debugRelease}-${variant}-${conf.fullVersionString}.apk")
            projectHelper.executeCommand(project,[
                'ant',
                "install${firstLetterLowerCase}",
                "-Pout.final.file=${apkFile}"
            ])
        }
        testTask.dependsOn(project.readAndroidProjectConfiguration)
    }

    private prepareInstallTaskNoVariant(Project project, String debugRelease) {
        def testTask = project.task("install${debugRelease}")
        testTask.description = "Installs " + debugRelease
        testTask.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
        def firstLetterLowerCase = debugRelease[0].toLowerCase()
        testTask << {
            File apkFile = new File(conf.targetDirectory,"${conf.projectName}-${debugRelease}-${conf.fullVersionString}.apk")
            projectHelper.executeCommand(project,[
                'ant',
                "install${firstLetterLowerCase}",
                "-Pout.final.file=${apkFile}"
            ])
        }
        testTask.dependsOn(project.readAndroidProjectConfiguration)
    }

    private void prepareAllInstallTasks(Project project) {
        if (androidBuilder.hasVariants()) {
            loopAllVariants(project,{ directory->
                prepareInstallTask(project, directory.name)
            })
        } else {
            prepareInstallTaskNoVariant(project, 'Debug')
            prepareInstallTaskNoVariant(project, 'Release')
        }
    }

    private void loopAllVariants(Project project, Closure closure) {
        androidBuilder.variantsDir.eachDir { directory ->
            if (!androidConf.isBuildExcluded(directory.name)) {
                closure (directory)
            }
        }
    }

    void prepareBuildDebugOnlyTask(Project project) {
        if (androidBuilder.hasVariants()) {
            def task = project.task('buildDebug')
            task.description = "Builds only debug variants"
            task.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
        }
    }

    void prepareBuildReleaseOnlyTask(Project project) {
        if (androidBuilder.hasVariants()) {
            def task = project.task('buildRelease')
            task.description = "Builds only release variants"
            task.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
        }
    }

    void prepareSingleVariant(Project project, String variant, String debugRelease) {
        if (variant != null && debugRelease == null) {
            debugRelease = androidBuilder.getDebugRelease(project, variant)
        }
        def noSpaceVariantName = variant == null? "" : '-' + variant.replaceAll(' ','_')
        def task = project.task("build${debugRelease}${noSpaceVariantName}")
        task.description = "Builds ${debugRelease}${noSpaceVariantName}"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
        task << {
            if (androidEnvironment.isLibrary()) {
                AndroidArtifactBuilderInfo bi = androidBuilder.buildJarArtifactBuilderInfo(project, variant, debugRelease)
                androidBuilder.buildSingleJar(bi)
            } else {
                AndroidArtifactBuilderInfo bi = androidBuilder.buildApkArtifactBuilderInfo(project, variant, debugRelease)
                androidBuilder.buildSingleApk(bi)
            }
        }
        task.dependsOn(project.readAndroidProjectConfiguration)
        if (variant != null) {
            project.tasks["build${debugRelease}"].dependsOn(task)
        }
    }


    void prepareBuildAllTask(Project project) {
        def task = project.task('buildAll')
        task.description = "Builds all variants"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
        task.dependsOn(project.tasks['buildDebug'],project.tasks['buildRelease'])
    }

    void prepareAllVariants(Project project) {
        if (androidBuilder.hasVariants()) {
            loopAllVariants (project,{ directory ->
                prepareSingleVariant(project, directory.name, null)
            })
        } else {
            ['Debug', 'Release'].each {
                prepareSingleVariant(project, null, it)
            }
        }
    }

    void prepareReadAndroidVersionAndProjectNameTask(Project project) {
        def task = project.task('readAndroidVersionAndProjectName')
        task.group = AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
        task.description = 'Reads Android version data from android manifest'
        task << {
            manifestHelper.readVersion(project.rootDir, conf)
            use (PropertyCategory) {
                if (!project.isPropertyOrEnvironmentVariableDefined('version.string')) {
                    logger.lifecycle("Version string is updated to SNAPSHOT because it is not release build")
                    conf.versionString = conf.versionString + "-SNAPSHOT"
                } else {
                    logger.lifecycle("Version string is not updated to SNAPSHOT because it is release build")
                }
                AndroidBuildXmlHelper buildXmlHelper = new AndroidBuildXmlHelper()
                project[ProjectConfigurationPlugin.PROJECT_NAME_PROPERTY] = buildXmlHelper.readProjectName(project.rootDir)
            }
        }
        project.readProjectConfiguration.dependsOn(task)
    }

    void prepareReadAndroidProjectConfigurationTask(Project project) {
        def task = project.task('readAndroidProjectConfiguration')
        task.group = AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
        task.description = 'Reads Android project configuration from properties'
        task << {
            androidBuilder.updateAndroidConfigurationWithVariants()
            androidConfRetriever.readAndroidProjectConfiguration(project)
        }
        task.dependsOn(project.readProjectConfiguration)
    }


    private void prepareReplacePackageTask(Project project) {
        def task = project.task('replacePackage')
        task.description = """Replaces manifest's package with a new one. Requires oldPackage and newPackage
           parameters. Optionally it takes newLabel or newName parameters if application's label/name is to be replaced"""
        task.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
        task << {
            use(PropertyCategory) {
                def oldPackage = project.readExpectedProperty("oldPackage")
                logger.lifecycle("Old package ${oldPackage}")
                def newPackage = project.readExpectedProperty("newPackage")
                logger.lifecycle("New package ${newPackage}")
                def newLabel = project.readProperty("newLabel")
                logger.lifecycle("New label ${newLabel}")
                def newName= project.readProperty("newLabel")
                logger.lifecycle("New name ${newName}")
                manifestHelper.replacePackage(project.getRootDir(), conf, oldPackage, newPackage, newLabel)
                logger.lifecycle("Replaced the package from ${oldPackage} to ${newPackage}")
                if (newLabel != null) {
                    logger.lifecycle("Also replaced label with ${newLabel}")
                }
                if (newName != null) {
                    logger.lifecycle("Replacing name with ${newName}")
                    AndroidBuildXmlHelper buildXMLHelper = new AndroidBuildXmlHelper()
                    buildXMLHelper.replaceProjectName(project.rootDir, newName)
                }
                File sourceFolder = new File("src/" + oldPackage.replaceAll('\\.', '/'))
                File targetFolder = new File("src/" + newPackage.replaceAll('\\.', '/'))
                logger.lifecycle("Moving ${sourceFolder} to ${targetFolder}")
                project.ant.move(file: sourceFolder, tofile : targetFolder, failonerror : false)
                logger.lifecycle("Replacing remaining references in AndroidManifest ")
                project.ant.replace(casesensitive: 'true', token : "${oldPackage}",
                        value: "${newPackage}", summary: true) {
                            fileset(dir: 'src') { include (name : '**/*.java') }
                            fileset(dir: 'res') { include (name : '**/*.xml') }
                            fileset(dir: '.') { include (name: 'AndroidManifest.xml')}
                        }
            }
        }
    }
}
