package com.apphance.ameba

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Abstract superclass for all 'verify' operations. It provides basic methods to be used
 * in all verify operations.
 */
abstract class AbstractVerifySetupOperation {


    List BOOLEANS = ['true', 'false']

    Logger logger = Logging.getLogger(AbstractVerifySetupOperation.class)
    final String propertyDescription
    final Class<? extends Enum> clazz
    Project project

    abstract void verifySetup()

    public AbstractVerifySetupOperation(Class<? extends Enum> clazz) {
        this.clazz = clazz
        this.propertyDescription = clazz.getField('DESCRIPTION').get(null)
    }

    protected Properties readProperties() {
        def projectProperties = new Properties()
        def projectPropertiesFile = project.file('gradle.properties')
        if (!projectPropertiesFile.exists()) {
            throw new GradleException("""The gradle.properties file does not exist.
!!!!! Please run "gradle prepareSetup --quiet" to correct project's configuration !!!!!""")
        }
        projectProperties.load(projectPropertiesFile.newInputStream())
        return projectProperties
    }

    protected static void checkProperty(Properties projectProperties, Enum property, List excludedProperties = null) {
        if (projectProperties.getProperty(property.propertyName) == null && property.defaultValue == null && !(property in excludedProperties)) {
            throw new GradleException("""Property ${property.propertyName} should be defined in gradle.properties.
!!!!! Please run "gradle prepareSetup --quiet" to correct it """)
        }
    }

    protected void allPropertiesOK() {
        logger.lifecycle("GOOD!!! ${propertyDescription} set correctly!!!")
    }

    protected void checkBoolean(property) {
        use(PropertyCategory) {
            String value = project.readProperty(property.propertyName)
            if (!BOOLEANS.contains(value)) {
                throw new GradleException("""The value in ${property.propertyName}: ${value} can only be one of ${BOOLEANS}""")
            }
        }
    }

    protected void checkIsOnList(property, list) {
        use(PropertyCategory) {
            String value = project.readProperty(property.propertyName)
            if (!list.any { it == value }) {
                throw new GradleException("""The value in ${property.propertyName}: ${value} can only be one of ${list}""")
            }
        }
    }

}
