package com.apphance.ameba.plugins.projectconfiguration

import java.util.Properties

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AmebaCommonBuildTaskGroups;


class VerifyBaseSetupTask extends DefaultTask {
    Logger logger = Logging.getLogger(VerifyBaseSetupTask.class)

    VerifyBaseSetupTask() {
        this.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        this.description = 'Verifies if base properties of the project have been setup properly'
        //inject myself as dependency for umbrella verifySetup
        project.verifySetup.dependsOn(this)
        this.dependsOn(project.readProjectConfiguration)
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
        for (ProjectBaseProperty property : ProjectBaseProperty.values()) {
            if (!property.isOptional() && project[property.getName()] == null) {
                throw new GradleException("""Property ${propertyName} should be defined in gradle.properties.
                !!!!! Please run "gradle prepareSetup" to correct it """)
            }
        }
        checkIconFile(projectProperties)
        logger.lifecycle("GOOD!!! PROJECT PROPERTIES SET CORRECTLY!!!")
        //        project['gradleProperties'] = projectProperties
    }

    void checkIconFile(Properties projectProperties) {
        File iconFile = new File(project.rootDir,projectProperties.getProperty('project.icon.file'))
        if (!iconFile.exists() || !iconFile.isFile()) {
            throw new GradleException("""The icon file (${iconFile}) does not exist
or is not a file. Please run 'gradle prepareSetup' to correct it.""")
        }
    }
}
