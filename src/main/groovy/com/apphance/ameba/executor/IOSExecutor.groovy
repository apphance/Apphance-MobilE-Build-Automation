package com.apphance.ameba.executor

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant
import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.ios.parsers.XCodeOutputParser
import groovy.transform.PackageScope

import javax.inject.Inject

import static com.apphance.ameba.configuration.ios.IOSConfiguration.PROJECT_PBXPROJ

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
        run('-showsdks')
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
                cmd: "plutil -convert json ${conf.xcodeDir.value}/$PROJECT_PBXPROJ -o -".split()))
    }()

    List<String> plistToJSON(File plist) {
        plistToJSONC(plist)
    }

    private Closure<List<String>> plistToJSONC = { File plist ->
        executor.executeCommand(new Command(
                runDir: conf.rootDir,
                cmd: "plutil -convert json ${plist.absolutePath} -o -".split()
        ))
    }.memoize()

    List<String> mobileprovisionToXml(File mobileprovision) {
        mobileProvisionToXmlC(mobileprovision)
    }

    private Closure<List<String>> mobileProvisionToXmlC = { File mobileprovision ->
        executor.executeCommand(new Command(
                runDir: conf.rootDir,
                cmd: "security cms -D -i ${mobileprovision.absolutePath}".split()
        ))
    }.memoize()

    Map<String, String> buildSettings(String target, String configuration) {
        buildSettingsC(target, configuration)
    }

    private Closure<Map<String, String>> buildSettingsC = { String target, String configuration ->
        def result = executor.executeCommand(new Command(
                runDir: conf.rootDir,
                cmd: conf.xcodebuildExecutionPath() + "-target $target -configuration $configuration -showBuildSettings".split().flatten()
        ))
        parser.parseBuildSettings(result)
    }.memoize()

    def clean() {
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: ['dot_clean', './']))
    }

    def buildVariant(File dir, List<String> buildCmd) {
        executor.executeCommand(new Command(runDir: dir, cmd: buildCmd))
    }

    def buildTestVariant(File dir, AbstractIOSVariant variant, String outputFilePath) {
        executor.executeCommand new Command(runDir: dir, cmd: variant.buildCmd() + [" -sdk $conf.simulatorSdk.value"],
                environment: [RUN_UNIT_TEST_WITH_IOS_SIM: 'YES', UNIT_TEST_OUTPUT_FILE: outputFilePath],
                failOnError: false
        )
    }

    List<String> run(String command) {
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: conf.xcodebuildExecutionPath() + command.split().flatten()))
    }
}
