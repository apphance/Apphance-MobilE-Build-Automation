package com.apphance.ameba.ios

import java.util.Properties

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AmebaCommonBuildTaskGroups


class IOSVerifySetupTask extends DefaultTask {
    Logger logger = Logging.getLogger(IOSVerifySetupTask.class)

    IOSVerifySetupTask() {
        this.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        this.description = 'Verifies if iOS-specific properties of the project have been setup properly'
        project.verifySetup.dependsOn(this)
    }


    @TaskAction
    void verifySetup() {
        Properties projectProperties = project['gradleProperties']
        checkProperty(projectProperties, 'ios.plist.file')
        checkPlistFile(projectProperties)
        checkProperty(projectProperties,'ios.families')
        checkFamilies(projectProperties)
        checkProperty(projectProperties,'ios.excluded.builds')
        checkProperty(projectProperties,'ios.distribution.resources.dir')
        checkDistributionDir(projectProperties)
        logger.lifecycle("GOOD!!! ALL IOS PROJECT PROPERTIES SET CORRECTLY!!!")
    }

    void checkPlistFile(Properties projectProperties) {
        File plistFile = new File(project.rootDir,projectProperties.getProperty('ios.plist.file'))
        if (!plistFile.exists() || !plistFile.isFile()) {
            throw new GradleException("""The plist file (${plistFile}) does not exist
or is not a file. Please run 'gradle prepareSetup' to correct it.""")
        }
    }

    void checkDistributionDir(Properties projectProperties) {
        File distributionResourcesDir = new File(project.rootDir,projectProperties.getProperty('ios.distribution.resources.dir'))
        if (!distributionResourcesDir.exists() || !distributionResourcesDir.isDirectory()) {
            throw new GradleException("""The distribution resources directory (${distributionResourcesDir})
does not exist or is not a directory. Please run 'gradle prepareSetup' to correct it.""")
        }
    }

    void checkFamilies(Properties projectProperties) {
        String[] families = projectProperties.getProperty('ios.families').split(',')
        def validFamilies = ['iPad', 'iPhone']
        families.each { family ->
            if (!validFamilies.contains(family)) {
                throw new GradleException("""The family in ${families} can only be one of ${validFamilies}""")
            }
        }
    }

    void checkProperty(Properties projectProperties, String propertyName) {
        if (projectProperties.getProperty(propertyName) == null) {
            throw new GradleException("""Property ${propertyName} should be defined in gradle.properties.
!!!!! Please run "gradle prepareSetup" to correct project's configuration !!!!!""")
        }
    }
}
