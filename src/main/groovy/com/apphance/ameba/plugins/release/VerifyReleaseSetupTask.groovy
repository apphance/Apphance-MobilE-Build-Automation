package com.apphance.ameba.plugins.release

import java.util.Properties;

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction
import org.gradle.api.logging.Logging

import com.apphance.ameba.AmebaCommonBuildTaskGroups;
import com.apphance.ameba.plugins.projectconfiguration.VerifyBaseSetupTask;


class VerifyReleaseSetupTask extends DefaultTask {

    Logger logger = Logging.getLogger(VerifyBaseSetupTask.class)

    VerifyReleaseSetupTask() {
        this.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        this.description = 'Verifies if release properties of the project have been setup properly'
        //inject myself as dependency for umbrella verifySetup
        project.verifySetup.dependsOn(this)
    }

    @TaskAction
    void verifySetup() {
        def projectProperties = new Properties()
        def projectPropertiesFile = new File(project.rootDir,'gradle.properties')
        if (!projectPropertiesFile.exists()) {
            throw new GradleException("""The gradle.properties file does not exist.
!!!!! Please run "gradle prepareSetup" to correct project's configuration !!!!!""")
        }
        projectProperties.load(projectPropertiesFile.newInputStream())
        logger.lifecycle(projectProperties.toString())
        for (ProjectReleaseProperty property : ProjectReleaseProperty.values()) {
            if (!property.optional) {
                checkProperty(projectProperties, property.propertyName)
            }
        }
        logger.lifecycle("GOOD!!! RELEASE PROPERTIES SET CORRECTLY!!!")
    }

    void checkProperty(Properties projectProperties, String propertyName) {
        if (projectProperties.getProperty(propertyName) == null) {
            throw new GradleException("""Property ${propertyName} should be defined in gradle.properties.
!!!!! Please run "gradle prepareSetup" to correct it """)
        }
    }
}
