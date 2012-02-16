package com.apphance.ameba.ios.plugins.framework


import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.AbstractVerifyIOSSetupTask
import com.apphance.ameba.ios.IOSProjectConfiguration;
import com.apphance.ameba.ios.IOSXCodeOutputParser


class VerifyFrameworkSetupTask extends AbstractVerifyIOSSetupTask {

    IOSXCodeOutputParser iosXCodeOutputParser
    IOSProjectConfiguration iosConf

    VerifyFrameworkSetupTask() {
        super(IOSFrameworkProperty.class)
        this.dependsOn(project.verifyIOSSetup)
    }

    @TaskAction
    void verifySetup() {
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
                    File file = new File(it)
                    if (!file.exists()) {
                        throw new GradleException("The file is missing in ${property.propertyName}: ${it}")
                    }
                }
            }
        }
    }
}
