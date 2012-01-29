package com.apphance.ameba

import java.util.Properties

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction


class VerifyBaseSetupTask extends DefaultTask {
    Logger logger = Logging.getLogger(VerifyBaseSetupTask.class)

    VerifyBaseSetupTask() {
        this.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        this.description = 'Verifies if base properties of the project have been setup properly'
        //inject myself as dependency for umbrella verifySetup
        project.verifySetup.dependsOn(this)
    }


    @TaskAction
    void verifySetup() {
        projectProperties = new Properties()
        def projectPropertiesFile = new File(project.rootDir,'gradle.properties')
        if (!projectPropertiesFile.exists()) {
            throw new GradleException("""The gradle.properties file does not exist.
!!!!! Please run "gradle prepareSetup" to correct project's configuration !!!!!""")
        }
        projectProperties.load(projectPropertiesFile.newInputStream())
        logger.lifecycle(projectProperties.toString())
        checkProperty(projectProperties, 'project.name')
        checkProperty(projectProperties, "project.icon.file")
        checkIconFile(projectProperties)
        checkProperty(projectProperties, 'project.url.base')
        checkProperty(projectProperties, 'project.directory.name')
        checkProperty(projectProperties, 'project.language')
        checkProperty(projectProperties, 'project.country')
        checkProperty(projectProperties, 'release.mail.from')
        checkProperty(projectProperties, 'release.mail.to')
        checkProperty(projectProperties, 'release.mail.subject')
        checkProperty(projectProperties, 'release.mail.flags')
        logger.lifecycle("GOOD!!! ALL PROJECT PROPERTIES SET CORRECTLY!!!")
        project['gradleProperties'] = projectProperties
    }

    void checkIconFile(Properties projectProperties) {
        File iconFile = new File(project.rootDir,projectProperties.getProperty('project.icon.file'))
        if (!iconFile.exists() || !iconFile.isFile()) {
            throw new GradleException("""The icon file (${iconFile}) does not exist
or is not a file. Please run 'gradle prepareSetup' to correct it.""")
        }
    }
    void checkProperty(Properties projectProperties, String propertyName) {
        if (projectProperties.getProperty(propertyName) == null) {
            throw new GradleException("""Property ${propertyName} should be defined in gradle.properties.
!!!!! Please run "gradle prepareSetup" to correct it """)
        }
    }
}
