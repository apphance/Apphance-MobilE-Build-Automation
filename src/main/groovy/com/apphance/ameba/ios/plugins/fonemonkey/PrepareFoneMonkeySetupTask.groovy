package com.apphance.ameba.ios.plugins.fonemonkey


import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractPrepareSetupTask;
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.IOSXCodeOutputParser

class PrepareFoneMonkeySetupTask extends AbstractPrepareSetupTask {

    Logger logger = Logging.getLogger(PrepareFoneMonkeySetupTask.class)
    ProjectConfiguration conf

    PrepareFoneMonkeySetupTask() {
        super(IOSFoneMonkeyProperty.class)
        this.dependsOn(project.prepareIOSSetup)
    }

    @TaskAction
    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        BufferedReader br = getReader()
        use (PropertyCategory) {
            IOSXCodeOutputParser iosXcodeOutputParser = new IOSXCodeOutputParser()
            IOSProjectConfiguration iosConf = iosXcodeOutputParser.getIosProjectConfiguration(project)
            IOSFoneMonkeyProperty.each {
                switch (it) {
                    case IOSFoneMonkeyProperty.FONE_MONKEY_CONFIGURATION:
                        project.getProjectPropertyFromUser(it, iosConf.allconfigurations, br)
                        break
                    default:
                        project.getProjectPropertyFromUser(it, null, br)
                }
            }
            appendProperties()
        }
    }
}
