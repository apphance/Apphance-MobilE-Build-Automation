package com.apphance.ameba.ios.plugins.buildplugin

import com.apphance.ameba.AbstractPrepareSetupOperation
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.IOSXCodeOutputParser
import com.apphance.ameba.util.file.FileManager
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Prepares IOS properties.
 *
 */
class PrepareIOSSetupOperation extends AbstractPrepareSetupOperation {
    Logger logger = Logging.getLogger(PrepareIOSSetupOperation.class)

    PrepareIOSSetupOperation() {
        super(IOSProjectProperty.class)
    }

    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        def plistFiles = FileManager.getFiles(project, { it.name.endsWith(".plist") })
        def xCodeProjFiles = FileManager.getDirectoriesSortedAccordingToDepth(project, { it.name.endsWith(".xcodeproj") })
        if (!xCodeProjFiles.empty && !project.hasProperty(IOSProjectProperty.PROJECT_DIRECTORY.propertyName)) {
            project.ext[IOSProjectProperty.PROJECT_DIRECTORY.propertyName] = xCodeProjFiles[0]
        }
        use(PropertyCategory) {
            IOSXCodeOutputParser iosXcodeOutputParser = new IOSXCodeOutputParser()
            IOSProjectConfiguration iosConf = iosXcodeOutputParser.getIosProjectConfiguration(project)
            BufferedReader br = getReader()
            IOSProjectProperty.each {
                switch (it) {
                    case IOSProjectProperty.PLIST_FILE:
                        project.getProjectPropertyFromUser(it, plistFiles, br)
                        break
                    case IOSProjectProperty.PROJECT_DIRECTORY:
                        project.getProjectPropertyFromUser(it, xCodeProjFiles, br)
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