package com.apphance.ameba

import java.util.List;
import java.util.Properties;

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

abstract class AbstractVerifySetupTask extends DefaultTask{

    List BOOLEANS = ['true', 'false']

    Logger logger = Logging.getLogger(AbstractVerifySetupTask.class)
    final String propertyDescription
    final Class<? extends Enum> clazz

    public AbstractVerifySetupTask(Class<? extends Enum> clazz) {
        this.clazz = clazz
        this.propertyDescription = clazz.getField('DESCRIPTION').get(null)
        this.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        this.description = "Verifies if ${propertyDescription} of the project are setup properly"
        // inject myself before the default
        project.verifySetup.dependsOn(this)
        this.dependsOn(project.readProjectConfiguration)
    }

    protected Properties readProperties() {
        def projectProperties = new Properties()
        def projectPropertiesFile = new File(project.rootDir,'gradle.properties')
        if (!projectPropertiesFile.exists()) {
            throw new GradleException("""The gradle.properties file does not exist.
!!!!! Please run "gradle prepareSetup --quiet" to correct project's configuration !!!!!""")
        }
        projectProperties.load(projectPropertiesFile.newInputStream())
        return projectProperties
    }

    protected static void checkProperty(Properties projectProperties, Enum property) {
        if (projectProperties.getProperty(property.propertyName) == null && property.defaultValue == null) {
            throw new GradleException("""Property ${property.propertyName} should be defined in gradle.properties.
!!!!! Please run "gradle prepareSetup --quiet" to correct it """)
        }
    }

    protected void allPropertiesOK() {
        logger.lifecycle("GOOD!!! ${propertyDescription} set correctly!!!")
    }

    protected void checkBoolean(property) {
        use (PropertyCategory) {
            String value = project.readProperty(property.propertyName)
            if (!BOOLEANS.contains(value)) {
                throw new GradleException("""The value in ${property.propertyName}: ${value} can only be one of ${BOOLEANS}""")
            }
        }
    }
}
