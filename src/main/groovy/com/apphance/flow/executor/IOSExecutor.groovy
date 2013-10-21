package com.apphance.flow.executor

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.executor.command.Command
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.plugins.ios.parsers.XCOutputParser
import groovy.transform.PackageScope

import javax.inject.Inject
import java.util.regex.Pattern

import static org.gradle.api.logging.Logging.getLogger

class IOSExecutor {

    private logger = getLogger(getClass())
    private final VERSION_PATTERN = Pattern.compile('(\\d+\\.)+\\d+')


    @Inject IOSConfiguration conf
    @Inject XCOutputParser parser
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
                cmd: ['xcodebuild', '-showsdks'])).toList()*.trim()
    }

    @Lazy List<String> schemes = {
        parser.readSchemes(list)
    }()

    @Lazy
    @PackageScope
    List<String> list = {
        executor.executeCommand(new Command(
                runDir: conf.rootDir,
                cmd: ['xcodebuild', '-list'])).toList()*.trim()
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
                cmd: ['xcodebuild', '-target', target, '-configuration', configuration, '-showBuildSettings']
        )).toList()
        parser.parseBuildSettings(result)
    }.memoize()

    def clean() {
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: ['dot_clean', './']))
    }

    Iterator<String> buildVariant(File dir, List<String> buildCmd) {
        executor.executeCommand(new Command(runDir: dir, cmd: buildCmd))
    }

    def runTestsLT5(File runDir, List<String> cmd, String testResultPath) {
        executor.executeCommand new Command(runDir: runDir, cmd: cmd, failOnError: false,
                environment: [RUN_UNIT_TEST_WITH_IOS_SIM: 'YES', UNIT_TEST_OUTPUT_FILE: testResultPath]
        )
    }

    def runTests5(File runDir, List<String> cmd) {
        executor.executeCommand new Command(runDir: runDir, cmd: cmd, failOnError: false)
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

    @Lazy
    String xCodeVersion = {
        def output = executor.executeCommand(new Command(
                runDir: conf.rootDir,
                cmd: ['xcodebuild', '-version']
        ))
        def line = output.find {
            it.matches('Xcode\\s+(\\d+\\.)+\\d+')
        }
        line ? line.split(' ')[1].trim() : null
    }()

    @Lazy
    String iOSSimVersion = {
        try {
            def output = executor.executeCommand(new Command(
                    runDir: conf.rootDir,
                    cmd: ['ios-sim', '--version']
            ))
            def line = output.find { it.matches('(\\d+\\.)+\\d+') }
            line ? line.trim() : ''
        } catch (Exception e) {
            logger.error("Error while getting ios-sim version: {}", e.message)
            ''
        }
    }()
}
