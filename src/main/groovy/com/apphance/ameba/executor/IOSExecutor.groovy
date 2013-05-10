package com.apphance.ameba.executor

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.ios.IOSXCodeOutputParser

import javax.inject.Inject

import static com.apphance.ameba.configuration.ios.IOSConfiguration.PROJECT_PBXPROJ

class IOSExecutor {

    @Inject
    IOSConfiguration conf
    @Inject
    IOSXCodeOutputParser parser
    @Inject
    CommandExecutor commandExecutor

    List<String> sdks() {
        parser.readIphoneSdks(showSdks()*.trim())
    }

    List<String> simulatorSdks() {
        parser.readIphoneSimulatorSdks(showSdks()*.trim())
    }

    private List<String> showSdks() {
        run('-showsdks')
    }

    List<String> targets() {
        parser.readBaseTargets(list())
    }

    List<String> configurations() {
        parser.readBaseConfigurations(list())
    }

    List<String> schemes() {
        parser.readSchemes(list())
    }

    List<String> list() {
        run('-list')*.trim()
    }

    List<String> pbxProjToXml() {
        commandExecutor.executeCommand(new Command(
                runDir: conf.rootDir,
                cmd: "plutil -convert xml1 -o - ${conf.xcodeDir.value}/$PROJECT_PBXPROJ".split()))
    }

    List<String> pbxProjToJSON() {
        commandExecutor.executeCommand(new Command(
                runDir: conf.rootDir,
                cmd: "plutil -convert json -o - ${conf.xcodeDir.value}/$PROJECT_PBXPROJ".split()))
    }

    def buildTarget(File dir, String target, String configuration, String sdk = conf.sdk.value, String params = "") {
        commandExecutor.executeCommand(new Command(runDir: dir, cmd:
                conf.xcodebuildExecutionPath() + "-target $target -configuration $configuration -sdk $sdk $params".split().flatten()))
    }

    def buildTestTarget(File dir, String target, String configuration, String outputFilePath) {
        commandExecutor.executeCommand(new Command(runDir: dir, cmd:
                conf.xcodebuildExecutionPath() + "-target $target -configuration $configuration -sdk $conf.simulatorSdk.value".split().flatten(),
                environment: [RUN_UNIT_TEST_WITH_IOS_SIM: 'YES', UNIT_TEST_OUTPUT_FILE: outputFilePath],
                failOnError: false
        ))
    }

    List<String> run(String command) {
        commandExecutor.executeCommand(new Command(runDir: conf.rootDir, cmd: conf.xcodebuildExecutionPath() + command.split().flatten()))
    }
}
