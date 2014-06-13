package com.apphance.flow.plugins.ios.apphance.source

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.properties.AbstractProperty
import com.apphance.flow.plugins.ios.apphance.pbx.IOSApphancePbxEnhancer
import com.google.inject.assistedinject.Assisted
import groovy.transform.PackageScope
import org.gradle.api.AntBuilder
import org.gradle.api.GradleException

import javax.inject.Inject

import static com.apphance.flow.configuration.apphance.ApphanceLibType.libForMode
import static com.apphance.flow.util.file.FileManager.MAX_RECURSION_LEVEL
import static com.google.common.base.Preconditions.*
import static groovy.io.FileType.FILES
import static java.util.regex.Pattern.compile
import static org.apache.commons.lang.StringUtils.isNotEmpty
import static org.gradle.api.logging.Logging.getLogger

class IOSApphanceSourceEnhancer {

    private final static DELEGATE_PATTERN = compile(".*<.*UIApplicationDelegate.*>.*")

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
        addApphanceToPch()
        replaceLogs()
        addApphanceInit()
    }

    @PackageScope
    void addApphanceToPch() {
        apphancePbxEnhancer.GCCPrefixFilePaths.each {
            def pch = new File(variant.tmpDir, it)
            logger.info("Adding apphance to PCH file : $pch.absolutePath")
            def old = pch.text
            pch.text = pch.text.replaceFirst("[\\t ]*#ifdef[\\t ]+__OBJC__", "\n#ifdef __OBJC__\n#import <$apphanceFrameworkName/APHLogger.h>")
            checkState(old != pch.text, "Unable to add APHLogger to pch file: $pch.absolutePath")
        }
    }

    private String getApphanceFrameworkName() {
        "Apphance-${libForMode(variant.aphMode.value).groupName.replace('p', 'P')}"
    }

    @PackageScope
    void replaceLogs() {
        replaceMLogs()
        replaceHLogs()
    }

    private void replaceMLogs() {
        logger.info("Replacing apphance logger in dir: $variant.tmpDir.absolutePath")
        apphancePbxEnhancer.filesToReplaceLogs.each { logger.info("Replacing NSLog with APHLog in: $it") }
        ant.replaceregexp(match: '\\bNSLog\\b', replace: 'APHLog', byline: true) {
            fileset(dir: variant.tmpDir) {
                apphancePbxEnhancer.filesToReplaceLogs.each {
                    include(name: it)
                }
            }
        }
    }

    private void replaceHLogs() {
        def hFilesToReplaceLogs = apphancePbxEnhancer.filesToReplaceLogs.collect { it.replace('.m', '.h') }.findAll {
            new File(variant.tmpDir, it).exists()
        }
        logger.info("Attempt to replace apphance logger for *.h files in dir: $variant.tmpDir.absolutePath")
        hFilesToReplaceLogs.each { logger.info("Replacing NSLog with APHLog in: $it") }
        ant.replaceregexp(match: '\\bNSLog\\b', replace: 'APHLog', byline: true) {
            fileset(dir: variant.tmpDir) {
                hFilesToReplaceLogs.each {
                    include(name: it)
                }
            }
        }
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
        variant.tmpDir.traverse([
                type: FILES,
                maxDepth: MAX_RECURSION_LEVEL,
                excludeFilter: ~/.*Pods.*/]) { f ->
            if (f.name.endsWith('.h') && f.readLines().find { l -> DELEGATE_PATTERN.matcher(l).matches() }) {
                appFilename = f.canonicalPath
            }
        }
        appFilename
    }

    @PackageScope
    void addApphanceToFile(File delegate) {
        logger.info("Adding apphance init in file: $delegate.absolutePath")
        def apphanceInit = """[APHLogger startNewSessionWithApplicationKey:@"$variant.aphAppKey.value"];"""
        def apphanceExceptionHandler = 'NSSetUncaughtExceptionHandler(&APHUncaughtExceptionHandler);'

        def splitLines = delegate.inject([]) { list, line -> list << line.split('\\s+') } as List
        def didFinishLine = splitLines.find { line -> line.join(' ').matches('.*application.*[dD]idFinishLaunching.*') } as List
        def didFinishLineIndex = splitLines.findIndexOf { it == didFinishLine }
        def bracketLineIndex = splitLines.findIndexOf(didFinishLineIndex, { line -> line.find { String token -> token.contains('{') } })
        def bracketLine = splitLines[bracketLineIndex] as List
        def bracketIndex = bracketLine.findIndexOf { String token -> token.contains('{') }
        def bracketToken = bracketLine[bracketIndex] as String
        bracketLine[bracketIndex] = bracketToken.replaceFirst('\\{', "\\{ \n $aphSettings \n $apphanceInit \n $apphanceExceptionHandler ")
        splitLines[bracketLineIndex] = bracketLine
        delegate.text = splitLines.collect { line -> line.join(' ') }.join('\n')
    }

    @PackageScope
    String getAphSettings() {
        [
                mapPropertyToAPHSettings(variant.aphReportOnShake, 'setReportOnShakeEnabled', boolCl),
                mapPropertyToAPHSettings(variant.aphWithUTest, 'setWithUTest', boolCl),
                mapPropertyToAPHSettings(variant.aphWithScreenShotsFromGallery, 'setScreenShotsFromGallery', boolCl),
                mapPropertyToAPHSettings(variant.aphReportOnDoubleSlide, 'setReportOnDoubleSlideEnabled', boolCl),
                mapPropertyToAPHSettings(variant.aphMachException, 'setMachExceptionEnabled', boolCl),
                mapPropertyToAPHSettings(variant.aphAppVersionCode, 'setApplicationVersionCode', stdCl),
                mapPropertyToAPHSettings(variant.aphAppVersionName, 'setApplicationVersionName', stdCl),
                mapPropertyToAPHSettings(variant.aphDefaultUser, 'setDefaultUser', stdCl),
                mapPropertyToAPHSettings(variant.aphServerURL, 'setServerURL', stdCl),
                mapPropertyToAPHSettings(variant.aphSendAllNSLogToApphance, 'setSendAllNSLogToApphance', boolCl),

        ].findAll { isNotEmpty(it) }.join('\n')
    }

    @PackageScope
    def boolCl = { AbstractProperty p, String method ->
        p.hasValue() ? """[[APHLogger defaultSettings] $method:${p.value ? 'YES' : 'NO'}];""" : ''
    }

    @PackageScope
    def stdCl = { AbstractProperty p, String method ->
        isNotEmpty(p.value?.toString()) ? """[[APHLogger defaultSettings] $method:@"${p.value.toString()}"];""" : ''
    }

    @PackageScope
    String mapPropertyToAPHSettings(AbstractProperty ap, String method, Closure c) {
        checkNotNull(ap, 'Null property passed')
        checkArgument(isNotEmpty(method), 'Empty method passed')
        c.call(ap, method)
    }
}
