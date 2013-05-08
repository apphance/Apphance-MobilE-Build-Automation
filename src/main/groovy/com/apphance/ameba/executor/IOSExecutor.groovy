package com.apphance.ameba.executor

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.ios.IOSXCodeOutputParser
import com.google.inject.Inject

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

    def list() {
        run('-list')
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
