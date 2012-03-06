package com.apphance.ameba.ios.plugins.buildplugin


import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AbstractPrepareSetupOperation
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSProjectConfiguration;
import com.apphance.ameba.ios.IOSXCodeOutputParser;


class PrepareIOSSetupOperation extends AbstractPrepareSetupOperation {
    Logger logger = Logging.getLogger(PrepareIOSSetupOperation.class)

    PrepareIOSSetupOperation() {
        super(IOSProjectProperty.class)
    }

    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        def plistFiles = getFiles {it.name.endsWith(".plist")}
        use (PropertyCategory) {
            IOSXCodeOutputParser iosXcodeOutputParser = new IOSXCodeOutputParser()
            IOSProjectConfiguration iosConf = iosXcodeOutputParser.getIosProjectConfiguration(project)
            BufferedReader br = getReader()
            IOSProjectProperty.each {
                switch (it) {
                    case IOSProjectProperty.PLIST_FILE:
                        project.getProjectPropertyFromUser(it, plistFiles, br)
                        break
                    case IOSProjectProperty.IOS_FAMILIES:
                        project.getProjectPropertyFromUser(it, IOSPlugin.FAMILIES, br)
                        break
                    case IOSProjectProperty.IOS_SDK:
                        project.getProjectPropertyFromUser(it, iosConf.allIphoneSDKs, br)
                        break
                    case IOSProjectProperty.IOS_SIMULATOR_SDK:
                        project.getProjectPropertyFromUser(it, iosConf.allIphoneSimulatorSDKs, br)
                        break
                    case IOSProjectProperty.MAIN_TARGET:
                        project.getProjectPropertyFromUser(it, iosConf.targets, br)
                        break
                    case IOSProjectProperty.MAIN_CONFIGURATION:
                        project.getProjectPropertyFromUser(it, iosConf.configurations, br)
                        break
                    default:
                        project.getProjectPropertyFromUser(it, null, br)
                }
            }
            appendProperties()
        }
    }
}