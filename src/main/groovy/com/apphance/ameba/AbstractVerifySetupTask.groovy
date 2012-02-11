package com.apphance.ameba

import java.util.Properties;

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

abstract class AbstractVerifySetupTask<T extends Enum> extends DefaultTask{

    Logger logger = Logging.getLogger(AbstractVerifySetupTask.class)
    final String propertyDescription

    public AbstractVerifySetupTask(Class<T> clazz) {
        this.propertyDescription = clazz.getField('DESCRIPTION').get(null)
        this.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        this.description = "Verifies if ${propertyDescription} of the project are setup properly"
        // inject myself before the default
        project.verifySetup.dependsOn(this)
    }

    protected Properties readProperties() {
        def projectProperties = new Properties()
        def projectPropertiesFile = new File(project.rootDir,'gradle.properties')
        if (!projectPropertiesFile.exists()) {
            throw new GradleException("""The gradle.properties file does not exist.
!!!!! Please run "gradle prepareSetup" to correct project's configuration !!!!!""")
        }
        projectProperties.load(projectPropertiesFile.newInputStream())
        return projectProperties
    }

    protected static void checkProperty(Properties projectProperties, Enum property) {
        if (projectProperties.getProperty(property.propertyName) == null) {
            throw new GradleException("""Property ${property.propertyName} should be defined in gradle.properties.
!!!!! Please run "gradle prepareSetup" to correct it """)
        }
    }

    protected void allPropertiesOK() {
        logger.lifecycle("GOOD!!! ${propertyDescription} set correctly!!!")
    }
}
