package com.apphance.ameba.executor

import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.ios.IOSProjectConfiguration
import com.google.inject.Inject
import org.gradle.api.Project

import static com.apphance.ameba.plugins.ios.buildplugin.IOSConfigurationRetriever.getIosProjectConfiguration

class IOSExecutor {

    @Inject
    CommandExecutor commandExecutor

    @Inject
    Project project

    IOSProjectConfiguration getIosConf() {
        getIosProjectConfiguration(project)
    }

    def showSdks() {
        run('-showsdks')
    }

    def list() {
        run('-list')
    }

    def buildTarget(File dir, String target, String configuration, String sdk = iosConf.sdk, String params = "") {
        commandExecutor.executeCommand(new Command(runDir: dir, cmd:
                iosConf.xCodeBuildExecutionPath(target, configuration) + "-target $target -configuration $configuration -sdk $sdk $params".split().flatten()))
    }

    def buildTestTarget(File dir, String target, String configuration, String outputFilePath) {
        commandExecutor.executeCommand(new Command(runDir: dir, cmd:
                iosConf.xCodeBuildExecutionPath(target, configuration) + "-target $target -configuration $configuration -sdk $iosConf.simulatorSDK".split().flatten(),
                environment: [RUN_UNIT_TEST_WITH_IOS_SIM: 'YES', UNIT_TEST_OUTPUT_FILE: outputFilePath],
                failOnError: false
        ))
    }

    List<String> run(String command) {
        commandExecutor.executeCommand(new Command(runDir: project.rootDir, cmd: iosConf.getXCodeBuildExecutionPath() + command.split().flatten()))
    }
}
