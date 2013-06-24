package com.apphance.flow.plugins.ios.apphance.source

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.plugins.ios.apphance.pbx.IOSApphancePbxEnhancer
import com.google.inject.assistedinject.Assisted
import groovy.transform.PackageScope
import org.gradle.api.AntBuilder
import org.gradle.api.GradleException

import javax.inject.Inject

import static com.apphance.flow.configuration.apphance.ApphanceLibType.libForMode
import static com.apphance.flow.configuration.apphance.ApphanceMode.*
import static com.apphance.flow.util.file.FileManager.MAX_RECURSION_LEVEL
import static groovy.io.FileType.FILES
import static org.gradle.api.logging.Logging.getLogger

class IOSApphanceSourceEnhancer {

    private logger = getLogger(getClass())

    private final static DELEGATE_PATTERN = ~/.*<.*UIApplicationDelegate.*>.*/

    @Inject AntBuilder ant

    @PackageScope AbstractIOSVariant variant
    @PackageScope IOSApphancePbxEnhancer apphancePbxEnhancer

    @Inject
    IOSApphanceSourceEnhancer(@Assisted AbstractIOSVariant variant, @Assisted IOSApphancePbxEnhancer apphancePbxEnhancer) {
        this.variant = variant
        this.apphancePbxEnhancer = apphancePbxEnhancer
    }

    void addApphanceToSource() {
        replaceLogs()
        addApphanceToPch()
        addApphanceInit()
    }

    @PackageScope
    void replaceLogs() {
        ant.replaceregexp(match: '^\\s+NSLog', replace: 'APHLog', byline: true) {
            fileset(dir: variant.tmpDir) {
                apphancePbxEnhancer.filesToReplaceLogs.each {
                    include(name: it)
                }
            }
        }
    }

    @PackageScope
    void addApphanceToPch() {
        def pch = new File(variant.tmpDir, apphancePbxEnhancer.GCCPrefixFilePath)
        pch.text = pch.text.replace("#ifdef __OBJC__", "#ifdef __OBJC__\n#import <$apphanceFrameworkName/APHLogger.h>")
    }

    private String getApphanceFrameworkName() {
        "Apphance-${libForMode(variant.apphanceMode.value).groupName.replace('p', 'P')}"
    }

    void addApphanceInit() {
        def appFilename = findAppDelegateFile()

        if (!appFilename) {
            throw new GradleException("Can not find UIApplicationDelegate file in dir: $variant.tmpDir.absolutePath")
        }

        appFilename = appFilename.replace('.h', '.m')
        File appDelegateFile = new File(appFilename)
        File newAppDelegateFile = new File("${appFilename}.tmp")
        newAppDelegateFile.delete()
        addApphanceToFile(appDelegateFile, newAppDelegateFile)
        appDelegateFile.delete()
        newAppDelegateFile.renameTo(appDelegateFile)
    }

    @PackageScope
    String findAppDelegateFile() {
        def appFilename = null
        variant.tmpDir.traverse([type: FILES, maxDepth: MAX_RECURSION_LEVEL]) {
            if (it.name.endsWith('.h') && it.filterLine { it ==~ DELEGATE_PATTERN }) {
                appFilename = it.canonicalPath
            }
        }
        appFilename
    }

    @PackageScope
    void addApphanceToFile(File appDelegateFile, File newAppDelegateFile) {
        boolean startNewSessionAdded = false
        boolean searchingForOpeningBrace = false
        def apphanceInit = """[APHLogger startNewSessionWithApplicationKey:@"$variant.apphanceAppKey.value" $apphanceMode];"""
        def apphanceExceptionHandler = 'NSSetUncaughtExceptionHandler(&APHUncaughtExceptionHandler);'
        newAppDelegateFile.withWriter { out ->
            appDelegateFile.eachLine { line ->
                if (line.matches('.*application.*[dD]idFinishLaunching.*')) {
                    searchingForOpeningBrace = true
                }
                if (!startNewSessionAdded && searchingForOpeningBrace && line.matches('.*\\{.*')) {
                    out.println(line.replace('{', "{ ${apphanceInit}${apphanceExceptionHandler}"))
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

    String getApphanceMode() {
        switch (variant.apphanceMode.value) {
            case QA:
                return 'apphanceMode:kAPHApphanceModeQA'
            case SILENT:
                return 'apphanceMode:kAPHApphanceModeSilent'
            case PROD:
                return ''
            default:
                throw new GradleException("Invalid apphance mode: '$variant.apphanceMode.value' for variant: '$variant.name'")
        }
    }

}
