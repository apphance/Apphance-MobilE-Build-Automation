package com.apphance.ameba.ios.plugins.apphance

import com.apphance.ameba.util.file.FileManager
import groovy.io.FileType
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class IOSApphanceSourceHelper {
    static Logger logger = Logging.getLogger(IOSApphanceSourceHelper.class)

    def findAppDelegateFile(File projectRootDirectory) {
        def appFilename = []
        projectRootDirectory.traverse([type: FileType.FILES, maxDepth: FileManager.MAX_RECURSION_LEVEL]) {
            if (it.name.endsWith(".h") && it.text.contains("UIApplicationDelegate")) {
                appFilename << it.canonicalPath
                logger.lifecycle("Application delegate found in file " + it)
            }
        }
        return appFilename
    }

    void addApphanceInit(File projectRootDirectory, String appKey, String apphanceMode = "apphanceMode:kAPHApphanceModeQA") {
        def appFilename = findAppDelegateFile(projectRootDirectory)

        if (appFilename.size() == 0) {
            throw new GradleException("Cannot find file with UIApplicationDelegate")
        }
        appFilename.each {
            it = it.replace(".h", ".m")
            File appDelegateFile = new File(it)
            File newAppDelegateFile = new File(it + ".tmp")
            if (appDelegateFile.exists()) {
                logger.lifecycle("Adding Apphance to ${it}")
                newAppDelegateFile.delete()
                addApphanceToFile(appDelegateFile, newAppDelegateFile, appKey, apphanceMode)
                appDelegateFile.delete()
                newAppDelegateFile.renameTo(appDelegateFile)
            } else {
                logger.warn("Could not find ${it} app delegate class. NOT ADDING Apphance initialisation!")
            }
        }
    }

    String addApphanceToFile(File appDelegateFile, File newAppDelegateFile, String appKey, String apphanceMode = "apphanceMode:kAPHApphanceModeQA") {
        boolean startNewSessionAdded = false
        boolean searchingForOpeningBrace = false
        def initApphance = "[APHLogger startNewSessionWithApplicationKey:@\"" + "${appKey}" + "\" $apphanceMode];"
        def setExceptionHandler = "NSSetUncaughtExceptionHandler(&APHUncaughtExceptionHandler);"
        newAppDelegateFile.withWriter { out ->
            appDelegateFile.eachLine { line ->
                if (line.matches('.*application.*[dD]idFinishLaunching.*')) {
                    searchingForOpeningBrace = true
                }
                if (!startNewSessionAdded && searchingForOpeningBrace && line.matches('.*\\{.*')) {
                    out.println(line.replace('{', "{ ${initApphance}${setExceptionHandler}"))
                    startNewSessionAdded = true
                } else {
                    out.println(line)
                }
            }
        }
        if (!startNewSessionAdded) {
            logger.warn("Could not find application's didFinishLaunching. Apphance not added")
        }
    }

    void addApphanceToPch(File pchFile, String apphanceFramework) {
        logger.lifecycle("Adding apphance header to file " + pchFile)
        File newPch = new File("newPch.pch")
        newPch.delete()
        newPch.withWriter { out ->
            out << pchFile.text.replace("#ifdef __OBJC__", "#ifdef __OBJC__\n#import <$apphanceFramework/APHLogger.h>")
        }
        pchFile.delete()
        pchFile.withWriter { out ->
            out << newPch.text
        }
        newPch.delete()
    }

}
