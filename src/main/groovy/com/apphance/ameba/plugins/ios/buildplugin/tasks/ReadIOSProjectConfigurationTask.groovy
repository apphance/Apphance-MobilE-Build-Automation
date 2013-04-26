package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.plugins.ios.IOSProjectConfiguration
import org.gradle.api.GradleException
import org.gradle.api.Project

import static com.apphance.ameba.PropertyCategory.readProperty
import static com.apphance.ameba.plugins.ios.IOSXCodeOutputParser.*
import static com.apphance.ameba.plugins.ios.buildplugin.IOSConfigurationRetriever.getIosProjectConfiguration
import static com.apphance.ameba.plugins.ios.buildplugin.IOSProjectProperty.*
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.PROJECT_NAME_PROPERTY
import static org.gradle.api.logging.Logging.getLogger

class ReadIOSProjectConfigurationTask {

    private l = getLogger(getClass())

    private Project project
    private IOSProjectConfiguration iosConf
    private IOSExecutor iosExecutor

    ReadIOSProjectConfigurationTask(Project project, IOSExecutor iosExecutor) {
        this.project = project
        this.iosExecutor = iosExecutor
        this.iosConf = getIosProjectConfiguration(project)
    }

    void readIosProjectConfiguration() {
        readProjectDirectory()
        readBasicIosProjectProperties()
        readConfiguration()
        readProjectConfigurationFromXCode()
        readVariantedProjectDirectories()
        readIOSParametersFromXCode()
    }

    private readProjectDirectory() {
        if (readProperty(project, PROJECT_DIRECTORY) != null) {
            iosConf.xCodeProjectDirectory = new File(project.rootDir, readProperty(project, PROJECT_DIRECTORY) as String)
        }
    }

    private readBasicIosProjectProperties() {
        iosConf.pListFileName = readProperty(project, PLIST_FILE)
        iosConf.excludedBuilds = readProperty(project, EXCLUDED_BUILDS).split(",")*.trim()
        iosConf.mainTarget = readProperty(project, MAIN_TARGET)
        iosConf.mainConfiguration = readProperty(project, MAIN_CONFIGURATION)
    }

    private void readConfiguration() {
        iosConf.sdk = readProperty(project, IOS_SDK)
        iosConf.simulatorSDK = readProperty(project, IOS_SIMULATOR_SDK)
        iosConf.plistFile = iosConf.pListFileName == null ? null : project.file(iosConf.pListFileName)
        String distDirName = readProperty(project, DISTRIBUTION_DIR)
        iosConf.distributionDirectory = distDirName == null ? null : project.file(distDirName)
        iosConf.families = readProperty(project, IOS_FAMILIES).split(",")*.trim()
    }

    private readProjectConfigurationFromXCode() {
        def trimmedListOutput = iosExecutor.list()*.trim()
        if (trimmedListOutput.empty || trimmedListOutput[0] == '') {//TODO possible?
            throw new GradleException("Error while running iosExecutor.list")
        }

        project.ext[PROJECT_NAME_PROPERTY] = readProjectName(trimmedListOutput)
        iosConf.targets = readBuildableTargets(trimmedListOutput)
        iosConf.configurations = readBuildableConfigurations(trimmedListOutput)
        iosConf.allTargets = readBaseTargets(trimmedListOutput, { true })
        iosConf.allConfigurations = readBaseConfigurations(trimmedListOutput, { true })
        iosConf.schemes = readSchemes(trimmedListOutput)

        def trimmedSdkOutput = iosExecutor.showSdks()*.trim()
        iosConf.allIphoneSDKs = readIphoneSdks(trimmedSdkOutput)
        iosConf.allIphoneSimulatorSDKs = readIphoneSimulatorSdks(trimmedSdkOutput)
    }

    private readVariantedProjectDirectories() {
        use(PropertyCategory) {
            if (project.readProperty(PROJECT_DIRECTORY) != null) {
                iosConf.allTargets.each { target ->
                    iosConf.allConfigurations.each { configuration ->
                        String variant = iosConf.getVariant(target, configuration)
                        iosConf.xCodeProjectDirectories[variant] = new File(tmpDir(target, configuration),
                                project.readProperty(PROJECT_DIRECTORY) as String)

                    }
                }
                l.info("Adding project directory: ${this.iosConf.mainTarget}-Debug")
                iosConf.xCodeProjectDirectories["${this.iosConf.mainTarget}-Debug".toString()] =
                    new File(tmpDir(this.iosConf.mainTarget, 'Debug'),
                            project.readProperty(PROJECT_DIRECTORY) as String)
            }
        }
    }

    private File tmpDir(String target, String configuration) {
        project.file("../tmp-${project.rootDir.name}-${target}-${configuration}")
    }

    private void readIOSParametersFromXCode() {
        project.file('bin').mkdirs()
        if (!iosConf.targets) {
            l.lifecycle("Please specify at least one target")
            iosConf.targets = []
        }
        if (!iosConf.configurations) {
            l.lifecycle("Please specify at least one configuration")
            iosConf.configurations = []
        }
        if (iosConf.mainTarget == null) {
            iosConf.mainTarget = iosConf.targets.empty ? null : iosConf.targets[0]
        }
        if (iosConf.mainConfiguration == null) {
            iosConf.mainConfiguration = iosConf.configurations.empty ? null : iosConf.configurations[0]
        }
        l.lifecycle("Standard buildable targets: " + iosConf.targets)
        l.lifecycle("Standard buildable configurations : " + iosConf.configurations)
        l.lifecycle("Main target: " + iosConf.mainTarget)
        l.lifecycle("Main configuration : " + iosConf.mainConfiguration)
        l.lifecycle("All targets: " + iosConf.allTargets)
        l.lifecycle("All configurations : " + iosConf.allConfigurations)
    }
}
