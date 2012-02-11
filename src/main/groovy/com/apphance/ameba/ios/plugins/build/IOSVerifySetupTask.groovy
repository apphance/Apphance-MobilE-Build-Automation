package com.apphance.ameba.ios.plugins.build


import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectHelper;
import com.apphance.ameba.ios.IOSConfigurationAndTargetRetriever;
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.IOSProjectProperty;


class IOSVerifySetupTask extends DefaultTask {
    Logger logger = Logging.getLogger(IOSVerifySetupTask.class)

    IOSVerifySetupTask() {
        this.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        this.description = 'Verifies if iOS-specific properties of the project have been setup properly'
        project.verifySetup.dependsOn(this)
    }


    @TaskAction
    void verifySetup() {
        for (IOSProjectProperty p : IOSProjectProperty.values()) {
            if (!p.isOptional()) {
                checkProperty(p.propertyName)
            }
        }
        checkPlistFile()
        checkFamilies()
        checkDistributionDir()
        checkTargets()
        logger.lifecycle("GOOD!!! IOS PROJECT PROPERTIES SET CORRECTLY!!!")
    }

    void checkPlistFile() {
        File plistFile = new File(project.rootDir,project[IOSProjectProperty.PLIST_FILE.propertyName])
        if (!plistFile.exists() || !plistFile.isFile()) {
            throw new GradleException("""The plist file (${plistFile}) does not exist
or is not a file. Please run 'gradle prepareSetup' to correct it.""")
        }
    }

    void checkDistributionDir() {
        File distributionResourcesDir = new File(project.rootDir,project[IOSProjectProperty.DISTRIBUTION_DIR.propertyName])
        if (!distributionResourcesDir.exists() || !distributionResourcesDir.isDirectory()) {
            throw new GradleException("""The distribution resources directory (${distributionResourcesDir})
does not exist or is not a directory. Please run 'gradle prepareSetup' to correct it.""")
        }
    }

    void checkFamilies() {
        String[] families = project[IOSProjectProperty.IOS_FAMILIES.propertyName].split(',')
        def validFamilies = ['iPad', 'iPhone']
        families.each { family ->
            if (!validFamilies.contains(family)) {
                throw new GradleException("""The family in ${families} can only be one of ${validFamilies}""")
            }
        }
    }

    void checkProperty(String propertyName) {
        if (project.getProperty(propertyName) == null) {
            throw new GradleException("""Property ${propertyName} should be defined in gradle.properties.
!!!!! Please run "gradle prepareSetup" to correct project's configuration !!!!!""")
        }
    }

    void checkTargets() {
        ProjectHelper projectHelper = new ProjectHelper();
        def lines = projectHelper.executeCommand(project, ["xcodebuild", "-list"]as String[],false, null, null, 1, true)
        def trimmed = lines*.trim()
        IOSConfigurationAndTargetRetriever iosConfigurationAndTargetRetriever = new IOSConfigurationAndTargetRetriever()
        IOSProjectConfiguration iosConf = iosConfigurationAndTargetRetriever.getIosProjectConfiguration(project)
        iosConf.targets = iosConfigurationAndTargetRetriever.readBuildableTargets(trimmed)
        iosConf.configurations = iosConfigurationAndTargetRetriever.readBuildableConfigurations(trimmed)
        iosConf.excludedBuilds = project.hasProperty(IOSProjectProperty.EXCLUDED_BUILDS.propertyName) ?
                project[IOSProjectProperty.EXCLUDED_BUILDS.propertyName].split(",")*.trim() : []
        if (iosConf.targets == ['']) {
            throw new GradleException("You must specify at least one target")
        }
        if (iosConf.configurations == ['']) {
            throw new GradleException("You must specify at least one configuration")
        }
        if (iosConf.excludedBuilds != ['.*']&& iosConf.excludedBuilds.size != ios.targets.size * ios.configurations.size) {
            def mainTarget
            if (!project.hasProperty(IOSProjectProperty.MAIN_TARGET.propertyName)) {
                mainTarget = project[IOSProjectProperty.MAIN_TARGET.propertyName]
            } else {
                mainTarget = iosConf.targets[0]
            }

            def mainConfiguration
            if (!project.hasProperty(IOSProjectProperty.MAIN_CONFIGURATION.propertyName)) {
                mainConfiguration = project[IOSProjectProperty.MAIN_CONFIGURATION.propertyName]
            } else {
                mainConfiguration = iosConf.configurations[0]
            }

            def id = "${mainTarget}-${mainConfiguration}".toString()

            if (iosConf.isBuildExcluded(id)) {
                throw new GradleException("Main target ${id} is excluded from build")
            }
        }
    }
}
