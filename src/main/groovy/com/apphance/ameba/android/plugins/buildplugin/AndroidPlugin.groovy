package com.apphance.ameba.android.plugins.buildplugin

import com.apphance.ameba.PluginHelper
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.android.*
import com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import static com.apphance.ameba.AmebaCommonBuildTaskGroups.AMEBA_BUILD
import static com.apphance.ameba.AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.PROJECT_NAME_PROPERTY

/**
 * Plugin for various Android related tasks.
 *
 */
class AndroidPlugin implements Plugin<Project> {

    static Logger logger = Logging.getLogger(AndroidPlugin.class)

    static final String PROJECT_PROPERTIES_KEY = 'project.properties'

    ProjectHelper projectHelper
    ProjectConfiguration conf
    AndroidProjectConfiguration androidConf
    AndroidManifestHelper manifestHelper
    AndroidSingleVariantApkBuilder androidApkBuilder
    AndroidSingleVariantJarBuilder androidJarBuilder
    AndroidEnvironment androidEnvironment

    @Override
    def void apply(Project project) {

        PluginHelper.checkAllPluginsAreLoaded(project, this.class, ProjectConfigurationPlugin.class)

        this.projectHelper = new ProjectHelper();
        this.conf = PropertyCategory.getProjectConfiguration(project)
        this.androidConf = AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration(project)
        this.manifestHelper = new AndroidManifestHelper()
        this.androidApkBuilder = new AndroidSingleVariantApkBuilder(project, this.androidConf)
        this.androidJarBuilder = new AndroidSingleVariantJarBuilder(project, this.androidConf)

        prepareCopySourcesTask(project)
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

        project.prepareSetup.prepareSetupOperations << new PrepareAndroidSetupOperation()
        project.verifySetup.verifySetupOperations << new VerifyAndroidSetupOperation()
        project.showSetup.showSetupOperations << new ShowAndroidSetupOperation()

        project.prepareSetup.dependsOn(project.readAndroidProjectConfiguration)
        project.verifySetup.dependsOn(project.readAndroidProjectConfiguration)
        project.showSetup.dependsOn(project.readAndroidProjectConfiguration)
    }

    void prepareCopySourcesTask(Project project) {
        def task = project.task('copySources')
        task.description = "Copies all sources to tmp directory for build"
        task.group = AMEBA_BUILD
        task << {
            androidConf.variants.each { variant ->
                project.ant.sync(toDir: androidConf.tmpDirs[variant], overwrite: true, failonerror: false, verbose: false) {
                    fileset(dir: "${project.rootDir}/") {
                        exclude(name: androidConf.tmpDirs[variant].absolutePath + '/**/*')
                        conf.sourceExcludes.each {
                            if (!it.equals('**/local.properties') && !it.equals('**/gen/**')) {
                                exclude(name: it)
                            }
                        }
                    }
                }
            }
        }
    }

    private void prepareAndroidEnvironment(Project project) {
        use(PropertyCategory) {
            logger.lifecycle("Running android update")
            runUpdateRecursively(project, project.rootDir, false, true)
            androidEnvironment = new AndroidEnvironment(project)
            def sdkDir = androidEnvironment.getAndroidProperty('sdk.dir')
            androidConf.sdkDirectory = sdkDir == null ? null : new File(sdkDir)
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
            androidConf.excludedBuilds = project.readProperty(AndroidProjectProperty.EXCLUDED_BUILDS).split(',')*.trim()
            def target = androidEnvironment.getAndroidProperty('target')
            if (target == null) {
                throw new GradleException("target is not defined. Please run 'android update project' or 'android create project' as appropriate")
            }
            androidConf.targetName = target
            androidConf.minSdkTargetName = project.readProperty(AndroidProjectProperty.MIN_SDK_TARGET)
            if (androidConf.minSdkTargetName.empty) {
                androidConf.minSdkTargetName = androidConf.targetName
            }
            logger.lifecycle("Min SDK target name = " + androidConf.minSdkTargetName)
            updateSdkJars()
            updateLibraryProjects(project.rootDir)
        }
    }

    private void updateSdkJars() {
        def target = androidConf.minSdkTargetName
        if (target.startsWith('android')) {
            String version = target.split("-")[1]
            androidConf.sdkJars << new File(androidConf.sdkDirectory, "platforms/android-" + version + "/android.jar")
        } else {
            List splitTarget = target.split(':')
            if (splitTarget.size() > 2) {
                String version = splitTarget[2]
                androidConf.sdkJars << new File(androidConf.sdkDirectory, "platforms/android-" + version + "/android.jar")
                if (target.startsWith('Google')) {
                    def mapJarFiles = new FileNameFinder().getFileNames(androidConf.sdkDirectory.path,
                            "add-ons/addon*google*apis*google*$version/libs/maps.jar")
                    for (path in mapJarFiles) {
                        androidConf.sdkJars << new File(path)
                    }
                }
                if (target.startsWith('KYOCERA Corporation:DTS')) {
                    androidConf.sdkJars << new File(androidConf.sdkDirectory, "add-ons/addon_dual_screen_apis_kyocera_corporation_" +
                            version + "/libs/dualscreen.jar")
                }
                if (target.startsWith('LGE:Real3D')) {
                    androidConf.sdkJars << new File(androidConf.sdkDirectory, "add-ons/addon_real3d_lge_" +
                            version + "/libs/real3d.jar")
                }
                if (target.startsWith('Sony Ericsson Mobile Communications AB:EDK')) {
                    androidConf.sdkJars << new File(androidConf.sdkDirectory, "add-ons/addon_edk_sony_ericsson_mobile_communications_ab_" +
                            version + "/libs/com.sonyericsson.eventstream_1.jar")
                }
            }
        }
        logger.lifecycle("Android SDK jars = " + androidConf.sdkJars)
    }

    private void updateLibraryProjects(File projectDir) {
        File libraryDir = new File(projectDir, "libs")
        if (libraryDir.exists()) {
            libraryDir.eachFile { file ->
                if (file.name.endsWith('.jar')) {
                    androidConf.libraryJars << file
                }
            }
        }
        Properties prop = new Properties()
        prop.load(new FileInputStream(new File(projectDir, PROJECT_PROPERTIES_KEY)))
        prop.each { key, value ->
            if (key.startsWith('android.library.reference.')) {
                File libraryProject = new File(projectDir, value)
                File binProject = new File(libraryProject, 'bin')
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

    private void prepareJavaEnvironment(Project project) {
        project.apply plugin: 'java'
        def javaConventions = project.convention.plugins.java
        javaConventions.sourceSets {
            main {
                output.classesDir = project.file('bin')
                output.resourcesDir = project.file('bin')
                java { srcDir project.file('src') }
                java { srcDir project.file('gen') }
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

    private prepareCompileAndroidTask(Project project) {
        def task = project.task('compileAndroid')
        task.description = "Performs code generation/compile tasks for android (if needed)"
        task.group = AMEBA_BUILD
        task << {
            logger.lifecycle("Prepares to compile Java for static code analysis")
            File gen = project.file('gen')
            if (!gen.exists() || gen.list().length == 0) {
                logger.lifecycle("Regenerating gen directory by running debug project")
                projectHelper.executeCommand(project, project.rootDir, [
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

    private void prepareUpdateProjectTask(Project project) {
        def task = project.task('updateProject')
        task.description = "updates project using android command line"
        task.group = AMEBA_BUILD
        task << {
            runUpdateRecursively(project, project.rootDir, true)
            logger.lifecycle("Performed android update")
        }
    }

    private void runUpdateRecursively(Project project, File currentDir, boolean reRun, boolean silentLogging = false) {
        runUpdateProject(project, currentDir, reRun)
        Properties prop = new Properties()
        File propFile = new File(currentDir, PROJECT_PROPERTIES_KEY)
        if (propFile.exists()) {
            prop.load(new FileInputStream(propFile))
            prop.each { key, value ->
                if (key.startsWith('android.library.reference.')) {
                    File libraryProject = new File(currentDir, value)
                    runUpdateRecursively(project, libraryProject, reRun, silentLogging)
                }
            }
        }
    }

    private runUpdateProject(Project project, File directory, boolean reRun, boolean silentLogging = false) {
        if (!new File(directory, 'local.properties').exists() || reRun) {
            if (!directory.exists()) {
                throw new GradleException("The directory ${directory} to execute the command, does not exist! Your configuration is wrong.")
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
        task.group = AMEBA_BUILD
        task << {
            projectHelper.executeCommand(project, [
                    'ant',
                    'clean'
            ])
            File tmpDir = project.file("tmp")
            project.ant.delete(dir: tmpDir)
        }
        project.clean.dependsOn(task)
        task.dependsOn(project.cleanConfiguration)
    }

    private void prepareCleanClassesTask(Project project) {
        def task = project.task('cleanClasses')
        task.description = "Cleans only the compiled classes"
        task.group = AMEBA_BUILD
        task << {
            project.ant.delete(dir: project.file("build"))
        }
    }

    void prepareReadAndroidVersionAndProjectNameTask(Project project) {
        def task = project.task('readAndroidVersionAndProjectName')
        task.group = AMEBA_CONFIGURATION
        task.description = 'Reads Android version data from android manifest'
        task << {
            manifestHelper.readVersion(project.rootDir, conf)
            use(PropertyCategory) {
                if (!project.isPropertyOrEnvironmentVariableDefined('version.string')) {
                    logger.lifecycle("Version string is updated to SNAPSHOT because it is not release build")
                    conf.versionString = conf.versionString + "-SNAPSHOT"
                } else {
                    conf.versionString = project.getPropertyOrEnvironmentVariableDefined('version.string')
                    logger.lifecycle("Version string is not updated to SNAPSHOT because it is release build. Given version is ${conf.versionString}")
                }
                AndroidBuildXmlHelper buildXmlHelper = new AndroidBuildXmlHelper()
                project.ext[PROJECT_NAME_PROPERTY] = buildXmlHelper.readProjectName(project.rootDir)
            }
        }
        project.readProjectConfiguration.dependsOn(task)
    }

    void prepareReadAndroidProjectConfigurationTask(Project project) {
        def task = project.task('readAndroidProjectConfiguration')
        task.group = AMEBA_CONFIGURATION
        task.description = 'Reads Android project configuration from properties'
        task << { AndroidProjectConfigurationRetriever.readAndroidProjectConfiguration(project) }
        androidApkBuilder.updateAndroidConfigurationWithVariants()
        task.dependsOn(project.readProjectConfiguration)
    }

    private void prepareAllInstallTasks(Project project) {
        if (androidApkBuilder.hasVariants()) {
            loopAllVariants({ directory ->
                prepareInstallTask(project, directory.name)
            }, false)
        } else {
            prepareInstallTaskNoVariant(project, 'Debug')
            prepareInstallTaskNoVariant(project, 'Release')
        }
    }

    private prepareInstallTask(Project project, String variant) {
        String debugRelease = androidConf.debugRelease[variant]
        def testTask = project.task("install${debugRelease}-${variant}")
        testTask.description = "Installs " + variant
        testTask.group = AMEBA_BUILD
        testTask << {
            def firstLetterLowerCase = debugRelease[0].toLowerCase()
            File apkFile = new File(conf.targetDirectory, "${conf.projectName}-${debugRelease}-${variant}-${conf.fullVersionString}.apk")
            projectHelper.executeCommand(project, [
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
        testTask.group = AMEBA_BUILD
        def firstLetterLowerCase = debugRelease[0].toLowerCase()
        testTask << {
            File apkFile = new File(conf.targetDirectory, "${conf.projectName}-${debugRelease}-${conf.fullVersionString}.apk")
            projectHelper.executeCommand(project, [
                    'ant',
                    "install${firstLetterLowerCase}",
                    "-Pout.final.file=${apkFile}"
            ])
        }
        testTask.dependsOn(project.readAndroidProjectConfiguration)
    }

    void prepareBuildDebugOnlyTask(Project project) {
        def task = project.task('buildAllDebug')
        task.description = "Builds only debug variants"
        task.group = AMEBA_BUILD
    }

    void prepareBuildReleaseOnlyTask(Project project) {
        def task = project.task('buildAllRelease')
        task.description = "Builds only release variants"
        task.group = AMEBA_BUILD
    }

    void prepareAllVariants(Project project) {
        if (androidApkBuilder.hasVariants()) {
            loopAllVariants({ directory ->
                prepareSingleVariant(project, directory.name, null)
            }, true)
        } else {
            ['Debug', 'Release'].each {
                prepareSingleVariant(project, it, it)
            }
        }
    }

    private void loopAllVariants(Closure closure, boolean printToOutput) {
        androidApkBuilder.variantsDir.eachDir { directory ->
            if (!androidConf.isBuildExcluded(directory.name)) {
                closure(directory)
            } else {
                if (printToOutput) {
                    println "Excluding variant ${directory.name} : excluded by configuration ${androidConf.excludedBuilds}"
                }
            }
        }
    }

    void prepareSingleVariant(Project project, String variant, String debugRelease) {
        if (variant != null && debugRelease == null) {
            debugRelease = androidConf.debugRelease[variant]
        }
        def noSpaceVariantName = '-' + variant.replaceAll(' ', '_')
        def task = project.task("build${debugRelease}${noSpaceVariantName}")
        task.description = "Builds ${debugRelease}${noSpaceVariantName}"
        task.group = AMEBA_BUILD
        task << {
            if (androidEnvironment.isLibrary()) {
                AndroidBuilderInfo bi = androidJarBuilder.buildJarArtifactBuilderInfo(project, variant, debugRelease)
                androidJarBuilder.buildSingle(bi)
            } else {
                AndroidBuilderInfo bi = androidApkBuilder.buildApkArtifactBuilderInfo(project, variant, debugRelease)
                androidApkBuilder.buildSingle(bi)
            }
        }
        task.dependsOn(project.readAndroidProjectConfiguration, project.verifySetup, project.copySources)
        project.tasks["buildAll${debugRelease}"].dependsOn(task)
    }

    void prepareBuildAllTask(Project project) {
        def task = project.task('buildAll')
        task.description = "Builds all variants"
        task.group = AMEBA_BUILD
        task.dependsOn(project.tasks['buildAllDebug'], project.tasks['buildAllRelease'])
    }

    private void prepareReplacePackageTask(Project project) {
        def task = project.task('replacePackage')
        task.description = """Replaces manifest's package with a new one. Requires oldPackage and newPackage
           parameters. Optionally it takes newLabel or newName parameters if application's label/name is to be replaced"""
        task.group = AMEBA_BUILD
        task << {
            use(PropertyCategory) {
                def oldPackage = project.readExpectedProperty("oldPackage")
                logger.lifecycle("Old package ${oldPackage}")
                def newPackage = project.readExpectedProperty("newPackage")
                logger.lifecycle("New package ${newPackage}")
                def newLabel = project.readProperty("newLabel")
                logger.lifecycle("New label ${newLabel}")
                def newName = project.readProperty("newLabel")
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
                File sourceFolder = project.file("src/" + oldPackage.replaceAll('\\.', '/'))
                File targetFolder = project.file("src/" + newPackage.replaceAll('\\.', '/'))
                logger.lifecycle("Moving ${sourceFolder} to ${targetFolder}")
                project.ant.move(file: sourceFolder, tofile: targetFolder, failonerror: false)
                logger.lifecycle("Replacing remaining references in AndroidManifest ")
                project.ant.replace(casesensitive: 'true', token: "${oldPackage}",
                        value: "${newPackage}", summary: true) {
                    fileset(dir: 'src') { include(name: '**/*.java') }
                    fileset(dir: 'res') { include(name: '**/*.xml') }
                    fileset(dir: '.') { include(name: 'AndroidManifest.xml') }
                }
            }
        }
    }

    private addAndroidSourceExcludes() {
        conf.sourceExcludes << '**/*.class'
        conf.sourceExcludes << '**/bin/**'
        conf.sourceExcludes << '**/gen/**'
        conf.sourceExcludes << '**/build/*'
        conf.sourceExcludes << '**/local.properties'
    }

    static public final String DESCRIPTION =
        """This is the main android build plugin.

The plugin provides all the tasks needed to build android application.
Besides tasks explained below, the plugin prepares build-* and install-*
tasks which are dynamically created, based on variants available. In
case the build has no variants, the only available builds are Debug and Release.
In case of variants, there is one build and one task created for every variant.
"""
}
