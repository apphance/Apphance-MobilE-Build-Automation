package com.apphance.flow.executor

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.executor.command.Command
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.plugins.ios.parsers.XCodeOutputParser
import groovy.transform.PackageScope

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSConfiguration.PROJECT_PBXPROJ

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
        run('-showsdks').toList()
    }

    @Lazy List<String> targets = {
        parser.readBaseTargets(list)
    }()

    @Lazy List<String> configurations = {
        parser.readBaseConfigurations(list)
    }()

    @Lazy List<String> schemes = {
        parser.readSchemes(list)
    }()

    @Lazy
    @PackageScope
    List<String> list = { run('-list')*.trim() }()

    @Lazy List<String> pbxProjToJSON = {
        executor.executeCommand(new Command(
                runDir: conf.rootDir,
                cmd: "plutil -convert json ${conf.xcodeDir.value}/$PROJECT_PBXPROJ -o -".split())).toList()
    }()

    List<String> plistToJSON(File plist) {
        plistToJSONC(plist)
    }

    private Closure<List<String>> plistToJSONC = { File plist ->
        executor.executeCommand(new Command(
                runDir: conf.rootDir,
                cmd: "plutil -convert json ${plist.absolutePath} -o -".split().toList()
        )).toList()
    }.memoize()

    List<String> plistToXML(File plistJSON) {
        executor.executeCommand(new Command(
                runDir: conf.rootDir,
                cmd: "plutil -convert xml1 ${plistJSON.absolutePath} -o -".split().toList()
        )).toList()
    }

    List<String> mobileprovisionToXml(File mobileprovision) {
        mobileProvisionToXmlC(mobileprovision)
    }

    private Closure<List<String>> mobileProvisionToXmlC = { File mobileprovision ->
        executor.executeCommand(new Command(
                runDir: conf.rootDir,
                cmd: "security cms -D -i ${mobileprovision.absolutePath}".split()
        )).toList()
    }.memoize()

    Map<String, String> buildSettings(String target, String configuration) {
        buildSettingsC(target, configuration)
    }

    private Closure<Map<String, String>> buildSettingsC = { String target, String configuration ->
        def result = executor.executeCommand(new Command(
                runDir: conf.rootDir,
                cmd: conf.xcodebuildExecutionPath() + "-target $target -configuration $configuration -showBuildSettings".split().flatten()
        )).toList()
        parser.parseBuildSettings(result)
    }.memoize()

    def clean() {
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: ['dot_clean', './']))
    }

    def buildVariant(File dir, List<String> buildCmd) {
        executor.executeCommand(new Command(runDir: dir, cmd: buildCmd))
    }

    def buildTestVariant(File dir, AbstractIOSVariant variant, String outputFilePath) {
        executor.executeCommand new Command(runDir: dir, cmd: variant.buildCmd(),
                environment: [RUN_UNIT_TEST_WITH_IOS_SIM: 'YES', UNIT_TEST_OUTPUT_FILE: outputFilePath],
                failOnError: false
        )
    }

    Iterator<String> run(String command) {
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: conf.xcodebuildExecutionPath() + command.split().flatten()))
    }
}
