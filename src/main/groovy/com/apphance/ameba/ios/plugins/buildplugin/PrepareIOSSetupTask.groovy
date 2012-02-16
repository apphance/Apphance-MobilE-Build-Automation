package com.apphance.ameba.ios.plugins.buildplugin



import groovy.io.FileType;

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction;

import com.apphance.ameba.AbstractPrepareSetupTask
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSProjectConfiguration;
import com.apphance.ameba.ios.IOSXCodeOutputParser;


class PrepareIOSSetupTask extends AbstractPrepareSetupTask {
    Logger logger = Logging.getLogger(PrepareIOSSetupTask.class)

    PrepareIOSSetupTask() {
        super(IOSProjectProperty.class)
        this.dependsOn(project.prepareBaseSetup)
    }

    @TaskAction
    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        def plistFiles = getPlistFiles()
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

    private List getPlistFiles() {
        def BIN_PATH = new File(project.rootDir,'bin').path
        def BUILD_PATH = new File(project.rootDir,'build').path
        def plistFiles = []
        project.rootDir.eachFileRecurse(FileType.FILES) {
            def thePath = it.path
            if (it.name.endsWith(".plist") && !thePath.startsWith(BIN_PATH) && !thePath.startsWith(BUILD_PATH)) {
                plistFiles << thePath
            }
        }
        return plistFiles
    }
}