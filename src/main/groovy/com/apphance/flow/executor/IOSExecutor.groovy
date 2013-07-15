package com.apphance.flow.executor

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.executor.command.Command
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.plugins.ios.parsers.XCodeOutputParser
import groovy.transform.PackageScope

import javax.inject.Inject

class IOSExecutor {

    @Inject IOSConfiguration conf
    @Inject XCodeOutputParser parser
    @Inject CommandExecutor executor

    @Lazy List<String> sdks = {
        parser.readIphoneSdks(showSdks()*.trim())
    }()

    @Lazy List<String> simulatorSdks = {
        parser.readIphoneSimulatorSdks(showSdks()*.trim())
    }()

    private List<String> showSdks() {
        executor.executeCommand(new Command(
                runDir: conf.rootDir,
                cmd: conf.xcodebuildExecutionPath() + ['-showsdks'])).toList()*.trim()
    }

    @Lazy List<String> schemes = {
        parser.readSchemes(list)
    }()

    @Lazy
    @PackageScope
    List<String> list = {
        executor.executeCommand(new Command(
                runDir: conf.rootDir,
                cmd: conf.xcodebuildExecutionPath() + ['-list'])).toList()*.trim()
    }()

    List<String> pbxProjToJSON(File pbxproj) {
        pbxProjToJSONC(pbxproj)
    }

    private Closure<List<String>> pbxProjToJSONC = { File pbxproj ->
        executor.executeCommand(new Command(
                runDir: pbxproj.parentFile,
                cmd: ['plutil', '-convert', 'json', pbxproj.absolutePath, '-o', '-']
        )).toList()
    }.memoize()

    List<String> plistToJSON(File plist) {
        plistToJSONC(plist)
    }

    private Closure<List<String>> plistToJSONC = { File plist ->
        executor.executeCommand(new Command(
                runDir: conf.rootDir,
                cmd: ['plutil', '-convert', 'json', plist.absolutePath, '-o', '-']
        )).toList()
    }.memoize()

    List<String> plistToXML(File plistJSON) {
        executor.executeCommand(new Command(
                runDir: conf.rootDir,
                cmd: ['plutil', '-convert', 'xml1', plistJSON.absolutePath, '-o', '-']
        )).toList()
    }

    List<String> mobileprovisionToXml(File mobileprovision) {
        mobileProvisionToXmlC(mobileprovision)
    }

    private Closure<List<String>> mobileProvisionToXmlC = { File mobileprovision ->
        executor.executeCommand(new Command(
                runDir: conf.rootDir,
                cmd: ['security', 'cms', '-D', '-i', mobileprovision.absolutePath]
        )).toList()
    }.memoize()

    Map<String, String> buildSettings(String target, String configuration) {
        buildSettingsC(target, configuration)
    }

    private Closure<Map<String, String>> buildSettingsC = { String target, String configuration ->
        def result = executor.executeCommand(new Command(
                runDir: conf.rootDir,
                cmd: conf.xcodebuildExecutionPath().toList() + ['-target', target, '-configuration', configuration, '-showBuildSettings']
        )).toList()
        parser.parseBuildSettings(result)
    }.memoize()

    def clean() {
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: ['dot_clean', './']))
    }

    def buildVariant(File dir, List<String> buildCmd) {
        executor.executeCommand(new Command(runDir: dir, cmd: buildCmd))
    }

    Iterator<String> archiveVariant(File dir, List<String> archiveCmd) {
        executor.executeCommand(new Command(runDir: dir, cmd: archiveCmd))
    }

    Iterator<String> dwarfdumpArch(File dSYM, String arch) {
        executor.executeCommand(new Command(
                runDir: dSYM.parentFile,
                cmd: ['dwarfdump', '--arch', arch, dSYM.absoluteFile]
        ))
    }

    Iterator<String> dwarfdumpUUID(File dSYM) {
        executor.executeCommand(new Command(
                runDir: dSYM.parentFile,
                cmd: ['dwarfdump', '-u', dSYM.absolutePath]
        ))
    }

    def buildTestVariant(File dir, IOSVariant variant, String outputFilePath) {
        executor.executeCommand new Command(runDir: dir, cmd: variant.buildCmd,
                environment: [RUN_UNIT_TEST_WITH_IOS_SIM: 'YES', UNIT_TEST_OUTPUT_FILE: outputFilePath],
                failOnError: false
        )
    }

    @Lazy
    String version = {
        def output = executor.executeCommand(new Command(
                runDir: conf.rootDir,
                cmd: ['xcodebuild', '-version']
        ))
        def line = output.find {
            it.matches('Xcode\\s+(\\d+\\.)+\\d+')
        }
        line ? line.split(' ')[1].trim() : null
    }()
}
