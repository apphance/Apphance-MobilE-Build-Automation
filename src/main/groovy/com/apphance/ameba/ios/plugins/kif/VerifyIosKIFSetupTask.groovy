package com.apphance.ameba.ios.plugins.kif


import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractVerifySetupTask
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSProjectConfiguration;
import com.apphance.ameba.ios.IOSXCodeOutputParser;


class VerifyIosKIFSetupTask extends AbstractVerifySetupTask {
    IOSXCodeOutputParser iosXCodeOutputParser
    IOSProjectConfiguration iosConf

    VerifyIosKIFSetupTask() {
        super(IOSKifProperty.class)
        this.dependsOn(project.verifyBaseSetup)
    }

    @TaskAction
    void verifySetup() {
        def projectProperties = readProperties()

        iosXCodeOutputParser = new IOSXCodeOutputParser()
        iosConf = iosXCodeOutputParser.getIosProjectConfiguration(project)
        IOSKifProperty.each {
            if (!it.defaultValue == null) {
                checkProperty(projectProperties, it)
            }
        }
        checkConfiguration()
        allPropertiesOK()
    }

    void checkConfiguration() {
        use (PropertyCategory) {
            String configuration = project.readProperty(IOSKifProperty.KIF_CONFIGURATION)
            if (!iosConf.allconfigurations.contains(configuration)) {
                throw new GradleException("""The kif configuration in ${IOSKifProperty.KIF_CONFIGURATION.propertyName}: ${configuration} can only be one of ${iosConf.allconfigurations}""")
            }
        }
    }
}
