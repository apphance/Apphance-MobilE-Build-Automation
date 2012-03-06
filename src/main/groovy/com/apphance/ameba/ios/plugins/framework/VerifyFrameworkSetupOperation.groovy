package com.apphance.ameba.ios.plugins.framework


import org.gradle.api.GradleException

import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.AbstractVerifyIOSSetupOperation
import com.apphance.ameba.ios.IOSProjectConfiguration;
import com.apphance.ameba.ios.IOSXCodeOutputParser


class VerifyFrameworkSetupOperation extends AbstractVerifyIOSSetupOperation {

    IOSXCodeOutputParser iosXCodeOutputParser
    IOSProjectConfiguration iosConf

    VerifyFrameworkSetupOperation() {
        super(IOSFrameworkProperty.class)
    }

    void verifySetup() {
        super.verifySetup()
        def projectProperties = readProperties()
        iosXCodeOutputParser = new IOSXCodeOutputParser()
        iosConf = iosXCodeOutputParser.getIosProjectConfiguration(project)

        IOSFrameworkProperty.each {
            if (!it.defaultValue != null) {
                checkProperty(projectProperties, it)
            }
        }
        checkTarget(IOSFrameworkProperty.FRAMEWORK_TARGET)
        checkConfiguration(IOSFrameworkProperty.FRAMEWORK_CONFIGURATION)
        checkFilesExist(IOSFrameworkProperty.FRAMEWORK_RESOURCES)
        checkFilesExist(IOSFrameworkProperty.FRAMEWORK_HEADERS)
        allPropertiesOK()
    }

    void checkFilesExist(property) {
        use (PropertyCategory) {
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
