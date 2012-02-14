package com.apphance.ameba.ios.plugins.framework


import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractVerifySetupTask
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSProjectConfiguration;
import com.apphance.ameba.ios.IOSXCodeOutputParser


class VerifyFrameworkSetupTask extends AbstractVerifySetupTask {

    IOSXCodeOutputParser iosXCodeOutputParser
    IOSProjectConfiguration iosConf

    VerifyFrameworkSetupTask() {
        super(IOSFrameworkProperty.class)
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
        checkTarget()
        checkConfiguration()
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


    void checkTarget() {
        use (PropertyCategory) {
            String target = project.readProperty(IOSFrameworkProperty.FRAMEWORK_TARGET)
            if (!iosConf.alltargets.contains(target)) {
                throw new GradleException("""The framework target in ${IOSFrameworkProperty.FRAMEWORK_TARGET.propertyName}: ${target} can only be one of ${iosConf.alltargets}""")
            }
        }
    }

    void checkConfiguration() {
        use (PropertyCategory) {
            String configuration = project.readProperty(IOSFrameworkProperty.FRAMEWORK_CONFIGURATION)
            if (!iosConf.allconfigurations.contains(configuration)) {
                throw new GradleException("""The framework configuration in ${IOSFrameworkProperty.FRAMEWORK_CONFIGURATION.propertyName}: ${configuration} can only be one of ${iosConf.allconfigurations}""")
            }
        }
    }
}
