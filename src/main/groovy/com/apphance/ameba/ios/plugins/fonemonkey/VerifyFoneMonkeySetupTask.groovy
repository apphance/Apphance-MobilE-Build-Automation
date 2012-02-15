package com.apphance.ameba.ios.plugins.fonemonkey


import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractVerifySetupTask
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSProjectConfiguration;
import com.apphance.ameba.ios.IOSXCodeOutputParser


class VerifyFoneMonkeySetupTask extends AbstractVerifySetupTask {

    VerifyFoneMonkeySetupTask() {
        super(IOSFoneMonkeyProperty.class)
        this.dependsOn(project.verifyIOSSetup)
    }

    @TaskAction
    void verifySetup() {
        def projectProperties = readProperties()
        def iosXCodeOutputParser = new IOSXCodeOutputParser()
        IOSProjectConfiguration iosConf = iosXCodeOutputParser.getIosProjectConfiguration(project)
        IOSFoneMonkeyProperty.each {
            if (!it.defaultValue != null) {
                checkProperty(projectProperties, it)
            }
        }
        use (PropertyCategory) {
            String configuration = project.readProperty(IOSFoneMonkeyProperty.FONE_MONKEY_CONFIGURATION)
            if (!iosConf.allconfigurations.contains(configuration)) {
                throw new GradleException("""The fonemonkey configuration in ${IOSFoneMonkeyProperty.FONE_MONKEY_CONFIGURATION.propertyName}: ${configuration} can only be one of ${iosConf.allconfigurations}""")
            }
        }

        allPropertiesOK()
    }
}
