package com.apphance.ameba.plugins.ios.framework

import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.plugins.ios.AbstractVerifyIOSSetupOperation
import com.apphance.ameba.plugins.ios.IOSProjectConfiguration
import org.gradle.api.GradleException

import static com.apphance.ameba.plugins.ios.buildplugin.IOSConfigurationRetriever.getIosProjectConfiguration
import static com.apphance.ameba.plugins.ios.framework.IOSFrameworkProperty.*

/**
 * Verifies properties for framework building.
 *
 */
class VerifyFrameworkSetupOperation extends AbstractVerifyIOSSetupOperation {

    IOSProjectConfiguration iosConf

    VerifyFrameworkSetupOperation() {
        super(IOSFrameworkProperty.class)
    }

    void verifySetup() {
        super.verifySetup()
        def projectProperties = readProperties()
        iosConf = getIosProjectConfiguration(project)

        IOSFrameworkProperty.each {
            if (!it.defaultValue != null) {
                checkProperty(projectProperties, it)
            }
        }
        checkTarget(FRAMEWORK_TARGET)
        checkConfiguration(FRAMEWORK_CONFIGURATION)
        checkFilesExist(FRAMEWORK_RESOURCES)
        checkFilesExist(FRAMEWORK_HEADERS)
        allPropertiesOK()
    }

    void checkFilesExist(property) {
        use(PropertyCategory) {
            String files = project.readProperty(property)
            if (files != '') {
                files.split(',').each {
                    File file = project.file(it)
                    if (!file.exists()) {
                        throw new GradleException("The file is missing in ${property.propertyName}: ${it}")
                    }
                }
            }
        }
    }
}
