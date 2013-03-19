package com.apphance.ameba.android.plugins.buildplugin

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.android.AndroidEnvironment
import com.apphance.ameba.android.AndroidProjectConfiguration
import com.apphance.ameba.android.plugins.buildplugin.tasks.*
import com.apphance.ameba.executor.AntExecutor
import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.AmebaCommonBuildTaskGroups.AMEBA_BUILD
import static com.apphance.ameba.AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
import static com.apphance.ameba.PropertyCategory.getProjectConfiguration
import static com.apphance.ameba.PropertyCategory.readProperty
import static com.apphance.ameba.android.AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration
import static com.apphance.ameba.android.AndroidProjectConfigurationRetriever.readAndroidProjectConfiguration
import static com.apphance.ameba.android.plugins.buildplugin.AndroidProjectProperty.EXCLUDED_BUILDS
import static com.apphance.ameba.android.plugins.buildplugin.AndroidProjectProperty.MIN_SDK_TARGET
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.*
import static org.gradle.api.logging.Logging.getLogger
import static org.gradle.api.plugins.BasePlugin.CLEAN_TASK_NAME
import static org.gradle.api.plugins.JavaPlugin.COMPILE_JAVA_TASK_NAME
import static org.gradle.api.plugins.JavaPlugin.JAVADOC_TASK_NAME

/**
 * Plugin for various Android related tasks.
 *
 */
class AndroidPlugin implements Plugin<Project> {

    private l = getLogger(getClass())

    public static final String PROJECT_PROPERTIES_KEY = 'project.properties'

    public static final String REPLACE_PACKAGE_TASK_NAME = 'replacePackage'
    public static final String BUILD_ALL_TASK_NAME = 'buildAll'
    public static final String BUILD_ALL_RELEASE_TASK_NAME = 'buildAllRelease'
    public static final String BUILD_ALL_DEBUG_TASK_NAME = 'buildAllDebug'
    public static final String COPY_SOURCES_TASK_NAME = 'copySources'
    public static final String COMPILE_ANDROID_TASK_NAME = 'compileAndroid'
    public static final String CLEAN_ANDROID_TASK_NAME = 'cleanAndroid'
    public static final String CLEAN_CLASSES_TASK_NAME = 'cleanClasses'
    public static final String READ_ANDROID_VERSION_AND_PROJECT_NAME_TASK_NAME = 'readAndroidVersionAndProjectName'
    public static final String READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME = 'readAndroidProjectConfiguration'
    public static final String UPDATE_PROJECT_TASK_NAME = 'updateProject'

    @Inject
    private CommandExecutor executor
    @Lazy
    private AntExecutor antExecutor = { new AntExecutor(project.rootDir) }()

    private Project project

    private ProjectConfiguration conf
    private AndroidProjectConfiguration androidConf
    private AndroidEnvironment androidEnvironment

    @Override
    void apply(Project project) {
        this.project = project

        this.conf = getProjectConfiguration(project)
        this.androidConf = getAndroidProjectConfiguration(project)
        this.androidEnvironment = new AndroidEnvironment(project)

        //TODO
        prepareJavaEnvironment()
        prepareAndroidEnvironment()
        addAndroidSourceExcludes()
        //TODO these three methods to auto detection

        prepareReadAndroidVersionAndProjectNameTask()
        prepareReadAndroidProjectConfigurationTask()
        prepareCleanClassesTask()
        prepareCleanTask()
        prepareCompileAndroidTask()
        prepareCopySourcesTask()
        prepareBuildAllTask()
        prepareBuildDebugOnlyTask()
        prepareBuildReleaseOnlyTask()
        prepareReplacePackageTask()
        prepareUpdateProjectTask()
        prepareAllVariants()
        prepareAllInstallTasks()

        project.prepareSetup.prepareSetupOperations << new PrepareAndroidSetupOperation()
        project.verifySetup.verifySetupOperations << new VerifyAndroidSetupOperation()
        project.showSetup.showSetupOperations << new ShowAndroidSetupOperation()

        project.prepareSetup.dependsOn(READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME)
        project.verifySetup.dependsOn(READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME)
        project.showSetup.dependsOn(READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME)
    }

    private void prepareJavaEnvironment() {
        project.plugins.apply('java')
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

    private void prepareAndroidEnvironment() {
        l.lifecycle("Running android update")
        new RunUpdateProjectTask(executor).runUpdateRecursively(project.rootDir, false)
        def sdkDir = androidEnvironment.getAndroidProperty('sdk.dir')
        androidConf.sdkDirectory = sdkDir == null ? null : new File(sdkDir)
        androidConf.rootDir = project.rootDir
        androidConf.variantsDir = project.file('variants')
        if (androidConf.sdkDirectory == null) {
            def androidHome = System.getenv('ANDROID_HOME')
            if (androidHome != null) {
                androidConf.sdkDirectory = new File(androidHome)
            }
        }
        if (androidConf.sdkDirectory == null) {
            throw new GradleException('Unable to find location of Android SDK, either\
 set it in local.properties or in ANDROID_HOME environment variable')
        }
        androidConf.excludedBuilds = readProperty(project, EXCLUDED_BUILDS).split(',')*.trim()
        def target = androidEnvironment.getAndroidProperty('target')
        if (target == null) {
            throw new GradleException("target is not defined. Please run 'android update project' or 'android create project' as appropriate")
        }
        androidConf.targetName = target
        androidConf.minSdkTargetName = readProperty(project, MIN_SDK_TARGET)
        if (androidConf.minSdkTargetName.empty) {
            androidConf.minSdkTargetName = androidConf.targetName
        }
        l.lifecycle("Min SDK target name: ${androidConf.minSdkTargetName}")

        updateSdkJars()
        updateLibraryProjects(project.rootDir)
        extractAvailableTargets()
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
        l.lifecycle("Android SDK jars = " + androidConf.sdkJars)
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

    private void extractAvailableTargets() {
        List<String> result = executor.executeCommand(new Command(runDir: project.rootDir, cmd: ['android', 'list', 'target']))
        androidConf.availableTargets = parseTargets(result.join('\n'))
    }

    private List<String> parseTargets(String output) {
        def targets = []
        def targetPattern = /id:.*"(.*)"/
        def targetPrefix = 'id:'
        output.eachLine {
            def targetMatcher = (it =~ targetPattern)
            if (it.startsWith(targetPrefix) && targetMatcher.matches()) {
                targets << targetMatcher[0][1]
            }
        }
        targets.sort()
    }

    private void addAndroidSourceExcludes() {
        conf.sourceExcludes << '**/*.class'
        conf.sourceExcludes << '**/bin/**'
        conf.sourceExcludes << '**/gen/**'
        conf.sourceExcludes << '**/build/*'
        conf.sourceExcludes << '**/local.properties'
    }

    private prepareReadAndroidVersionAndProjectNameTask() {
        def task = project.task(READ_ANDROID_VERSION_AND_PROJECT_NAME_TASK_NAME)
        task.group = AMEBA_CONFIGURATION
        task.description = 'Reads Android version data from android manifest'
        task << { new ReadAndroidVersionAndProjectTask(project).readAndroidVersionAndProjectTask() }
        project.tasks[READ_PROJECT_CONFIGURATION_TASK_NAME].dependsOn(task)
    }

    private void prepareReadAndroidProjectConfigurationTask() {
        def task = project.task(READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME)
        task.group = AMEBA_CONFIGURATION
        task.description = 'Reads Android project configuration from properties'
        task << { readAndroidProjectConfiguration(project) }
        task.dependsOn(READ_PROJECT_CONFIGURATION_TASK_NAME)
    }

    private void prepareCleanClassesTask() {
        def task = project.task(CLEAN_CLASSES_TASK_NAME)
        task.description = 'Cleans only the compiled classes'
        task.group = AMEBA_BUILD
        task << { new CleanClassesTask(project).cleanClasses() }
    }

    private void prepareCleanTask() {
        def task = project.task(CLEAN_ANDROID_TASK_NAME)
        task.description = 'Cleans the application'
        task.group = AMEBA_BUILD
        task << { new CleanAndroidTask(project, antExecutor).cleanAndroid() }
        project.tasks[CLEAN_TASK_NAME].dependsOn(CLEAN_ANDROID_TASK_NAME)
        task.dependsOn(CLEAN_CONFIGURATION_TASK_NAME)
    }

    private void prepareCompileAndroidTask() {
        def task = project.task(COMPILE_ANDROID_TASK_NAME)
        task.description = 'Performs code generation/compile tasks for android (if needed)'
        task.group = AMEBA_BUILD
        task << { new CompileAndroidTask(project, antExecutor).compileAndroid() }
        project.tasks[JAVADOC_TASK_NAME].dependsOn(COMPILE_ANDROID_TASK_NAME)
        project.tasks[COMPILE_JAVA_TASK_NAME].dependsOn(COMPILE_ANDROID_TASK_NAME)
    }

    private void prepareCopySourcesTask() {
        def task = project.task(COPY_SOURCES_TASK_NAME)
        task.description = 'Copies all sources to tmp directory for build'
        task.group = AMEBA_BUILD
        task << { new CopySourcesTask(project).copySources() }
    }

    private void prepareBuildAllTask() {
        def task = project.task(BUILD_ALL_TASK_NAME)
        task.description = 'Builds all variants'
        task.group = AMEBA_BUILD
        task.dependsOn(BUILD_ALL_DEBUG_TASK_NAME, BUILD_ALL_RELEASE_TASK_NAME)
    }

    private void prepareBuildDebugOnlyTask() {
        def task = project.task(BUILD_ALL_DEBUG_TASK_NAME)
        task.description = "Builds only debug variants"
        task.group = AMEBA_BUILD
    }

    private void prepareBuildReleaseOnlyTask() {
        def task = project.task(BUILD_ALL_RELEASE_TASK_NAME)
        task.description = "Builds only release variants"
        task.group = AMEBA_BUILD
    }

    private void prepareReplacePackageTask() {
        def task = project.task(REPLACE_PACKAGE_TASK_NAME)
        task.description = """Replaces manifest's package with a new one. Requires oldPackage and newPackage
           parameters. Optionally it takes newLabel or newName parameters if application's label/name is to be replaced"""
        task.group = AMEBA_BUILD
        task << { new ReplacePackageTask(project).replacePackage() }
    }

    private void prepareUpdateProjectTask() {
        def task = project.task(UPDATE_PROJECT_TASK_NAME)
        task.description = 'Updates project using android command line tool'
        task.group = AMEBA_BUILD
        task << { new RunUpdateProjectTask(executor).runUpdateRecursively(project.rootDir, true) }
    }

    private void prepareAllVariants() {
        if (androidConf.hasVariants()) {
            androidConf.variantsDir.eachDir { dir ->
                if (!androidConf.isBuildExcluded(dir.name)) {
                    prepareSingleVariant(dir.name, null)
                }
            }
        } else {
            ['Debug', 'Release'].each {
                prepareSingleVariant(it, it)
            }
        }
    }

    private void prepareSingleVariant(String variant, String debugRelease) {
        if (variant != null && debugRelease == null) {
            debugRelease = androidConf.debugRelease[variant]
        }
        def noSpaceVariantName = '-' + variant.replaceAll(' ', '_')
        def task = project.task("build${debugRelease}${noSpaceVariantName}")
        task.description = "Builds ${debugRelease}${noSpaceVariantName}"
        task.group = AMEBA_BUILD
        task << { new SingleVariantTask(project, executor, androidEnvironment).singleVariant(variant, debugRelease) }
        task.dependsOn(READ_PROJECT_CONFIGURATION_TASK_NAME, VERIFY_SETUP_TASK_NAME, COPY_SOURCES_TASK_NAME)
        project.tasks["buildAll$debugRelease"].dependsOn(task)
    }

    private void prepareAllInstallTasks() {
        if (androidConf.hasVariants()) {
            androidConf.variantsDir.eachDir { dir ->
                if (!androidConf.isBuildExcluded(dir.name)) {
                    prepareInstallTask(dir.name)
                }
            }
        } else {
            prepareInstallTaskNoVariant('Debug')
            prepareInstallTaskNoVariant('Release')
        }
    }

    private void prepareInstallTask(String variant) {
        String debugRelease = androidConf.debugRelease[variant]
        def task = project.task("install${debugRelease}-${variant}")
        task.description = "Installs $variant"
        task.group = AMEBA_BUILD
        task << { new InstallTask(project, antExecutor).install(variant, debugRelease) }
        task.dependsOn(READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME)
    }

    private void prepareInstallTaskNoVariant(String debugRelease) {
        def task = project.task("install${debugRelease}")
        task.description = "Installs $debugRelease"
        task.group = AMEBA_BUILD
        task << { new InstallTaskNoVariant(project, antExecutor).install(debugRelease) }
        task.dependsOn(READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME)
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
