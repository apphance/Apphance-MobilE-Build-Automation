package com.apphance.ameba.ios.plugins.buildplugin

import org.gradle.api.GradleException
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractVerifySetupTask
import com.apphance.ameba.ProjectHelper;
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSConfigurationAndTargetRetriever
import com.apphance.ameba.ios.IOSProjectConfiguration


class VerifyIOSSetupTask extends AbstractVerifySetupTask {
    Logger logger = Logging.getLogger(VerifyIOSSetupTask.class)

    VerifyIOSSetupTask() {
        super(IOSProjectProperty.class)
    }


    @TaskAction
    void verifySetup() {
        def projectProperties = readProperties()
        IOSProjectProperty.each{ checkProperty(projectProperties, it) }
        checkPlistFile()
        checkFamilies()
        checkDistributionDir()
        checkTargetsAndConfigurations()
        allPropertiesOK()
    }

    void checkPlistFile() {
        use (PropertyCategory) {
            File plistFile = new File(project.rootDir,project.readExpectedProperty(IOSProjectProperty.PLIST_FILE))
            if (!plistFile.exists() || !plistFile.isFile()) {
                throw new GradleException("""The plist file (${plistFile}) does not exist
 or is not a file. Please run 'gradle prepareSetup' to correct it.""")
            }
        }
    }

    void checkDistributionDir() {
        use (PropertyCategory) {
            File distributionResourcesDir = new File(project.rootDir,project.readExpectedProperty(IOSProjectProperty.DISTRIBUTION_DIR))
            if (!distributionResourcesDir.exists() || !distributionResourcesDir.isDirectory()) {
                throw new GradleException("""The distribution resources directory (${distributionResourcesDir})
does not exist or is not a directory. Please run 'gradle prepareSetup' to correct it.""")
            }
            boolean hasMobileProvision = false
            distributionResourcesDir.list().each {
                if (it.endsWith('.mobileprovision')) {
                    hasMobileProvision = true
                }
            }
            if (!hasMobileProvision) {
                throw new GradleException("""The distribution resources directory (${distributionResourcesDir})
should contain at least one .mobileprovision file. """)
            }
        }
    }

    void checkFamilies() {
        use (PropertyCategory) {
            String[] families = project.readProperty(IOSProjectProperty.IOS_FAMILIES).split(',')
            def validFamilies = ['iPad', 'iPhone']
            families.each { family ->
                if (!validFamilies.contains(family)) {
                    throw new GradleException("""The family in ${families} can only be one of ${validFamilies}""")
                }
            }
        }
    }

    void checkTargetsAndConfigurations() {
        use (PropertyCategory) {
            ProjectHelper projectHelper = new ProjectHelper();
            def lines = projectHelper.executeCommand(project, ["xcodebuild", "-list"]as String[],false, null, null, 1, true)
            def trimmed = lines*.trim()
            IOSConfigurationAndTargetRetriever iosConfigurationAndTargetRetriever = new IOSConfigurationAndTargetRetriever()
            IOSProjectConfiguration iosConf = iosConfigurationAndTargetRetriever.getIosProjectConfiguration(project)
            iosConf.targets = iosConfigurationAndTargetRetriever.readBuildableTargets(trimmed)
            iosConf.configurations = iosConfigurationAndTargetRetriever.readBuildableConfigurations(trimmed)
            iosConf.excludedBuilds = project.readProperty(IOSProjectProperty.EXCLUDED_BUILDS).split(",")*.trim()
            if (iosConf.targets == ['']) {
                throw new GradleException("You must specify at least one target")
            }
            if (iosConf.configurations == ['']) {
                throw new GradleException("You must specify at least one configuration")
            }
            if (iosConf.excludedBuilds != ['.*']&& iosConf.excludedBuilds.size != iosConf.targets.size * iosConf.configurations.size) {
                def mainTarget = readMainTarget(iosConf)
                if (!iosConf.targets.contains(mainTarget)) {
                    throw new GradleException("Main target ${mainTarget} is not on the list of targets ${iosConf.targets}")
                }
                def mainConfiguration = readMainConfiguration(iosConf)
                if (!iosConf.configurations.contains(mainConfiguration)) {
                    throw new GradleException("Main configuration ${mainConfiguration} is not on the list of targets ${iosConf.configurations}")
                }
                def id = "${mainTarget}-${mainConfiguration}".toString()
                if (iosConf.isBuildExcluded(id)) {
                    throw new GradleException("Main target-configuration pair (${id}) is excluded from build by ${iosConf.excludedBuilds}")
                }
            }
        }
    }

    private String readMainConfiguration(IOSProjectConfiguration iosConf) {
        def mainConfiguration
        if (!project.hasProperty(IOSProjectProperty.MAIN_CONFIGURATION.propertyName)) {
            mainConfiguration = project.readProperty(IOSProjectProperty.MAIN_CONFIGURATION)
        } else {
            mainConfiguration = iosConf.configurations[0]
        }
        return mainConfiguration
    }

    private String readMainTarget(IOSProjectConfiguration iosConf) {
        def mainTarget
        if (!project.hasProperty(IOSProjectProperty.MAIN_TARGET.propertyName)) {
            mainTarget = project.readProperty(IOSProjectProperty.MAIN_TARGET)
        } else {
            mainTarget = iosConf.targets[0]
        }
        return mainTarget
    }
}
