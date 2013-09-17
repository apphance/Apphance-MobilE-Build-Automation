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

    private final static DELEGATE_PATTERN = /.*<.*UIApplicationDelegate.*>.*/
    private logger = getLogger(getClass())

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
        logger.info("Replacing apphance logger in dir: $variant.tmpDir.absolutePath")
        logger.info("Source files to replace logs: ${apphancePbxEnhancer.filesToReplaceLogs}")
        ant.replaceregexp(match: '\\bNSLog\\b', replace: 'APHLog', byline: true) {
            fileset(dir: variant.tmpDir) {
                apphancePbxEnhancer.filesToReplaceLogs.each {
                    include(name: it)
                }
            }
        }
    }

    @PackageScope
    void addApphanceToPch() {
        apphancePbxEnhancer.GCCPrefixFilePaths.each {
            def pch = new File(variant.tmpDir, it)
            logger.info("Adding apphance to PCH file : $pch.absolutePath")
            pch.text = pch.text.replace("#ifdef __OBJC__", "#ifdef __OBJC__\n#import <$apphanceFrameworkName/APHLogger.h>")
        }
    }

    private String getApphanceFrameworkName() {
        "Apphance-${libForMode(variant.apphanceMode.value).groupName.replace('p', 'P')}"
    }

    @PackageScope
    void addApphanceInit() {
        def appFilename = findAppDelegateFile()

        if (!appFilename) {
            throw new GradleException("Can not find UIApplicationDelegate file in dir: $variant.tmpDir.absolutePath")
        }

        appFilename = appFilename.replace('.h', '.m')
        addApphanceToFile(new File(appFilename))
    }

    @PackageScope
    String findAppDelegateFile() {
        def appFilename = null
        variant.tmpDir.traverse([type: FILES, maxDepth: MAX_RECURSION_LEVEL]) {
            if (it.name.endsWith('.h') && it.readLines().find { it =~ DELEGATE_PATTERN }) {
                appFilename = it.canonicalPath
            }
        }
        appFilename
    }

    @PackageScope
    void addApphanceToFile(File delegate) {
        logger.info("Adding apphance init in file: $delegate.absolutePath")
        def apphanceInit = """[APHLogger startNewSessionWithApplicationKey:@"$variant.apphanceAppKey.value"];"""
        def apphanceExceptionHandler = 'NSSetUncaughtExceptionHandler(&APHUncaughtExceptionHandler);'

        def splitLines = delegate.inject([]) { list, line -> list << line.split('\\s+') } as List
        def didFinishLine = splitLines.find { line -> line.join(' ').matches('.*application.*[dD]idFinishLaunching.*') } as List
        def didFinishLineIndex = splitLines.findIndexOf { it == didFinishLine }
        def bracketLineIndex = splitLines.findIndexOf(didFinishLineIndex, { line -> line.find { String token -> token.contains('{') } })
        def bracketLine = splitLines[bracketLineIndex] as List
        def bracketIndex = bracketLine.findIndexOf { String token -> token.contains('{') }
        def bracketToken = bracketLine[bracketIndex] as String
        bracketLine[bracketIndex] = bracketToken.replaceFirst('\\{', "\\{ \n $apphanceInit \n $apphanceMode \n $apphanceExceptionHandler ")
        splitLines[bracketLineIndex] = bracketLine
        delegate.text = splitLines.collect { line -> line.join(' ') }.join('\n')
    }

    String getApphanceMode() {
        switch (variant.apphanceMode.value) {
            case QA:
                return """[[APHLogger defaultSettings] setApphanceMode:APHSettingsModeQA];"""
            case SILENT:
                return """[[APHLogger defaultSettings] setApphanceMode:APHSettingsModeSilent];"""
            case PROD:
                return ''
            default:
                throw new GradleException("Invalid apphance mode: '$variant.apphanceMode.value' for variant: '$variant.name'")
        }
    }

}
