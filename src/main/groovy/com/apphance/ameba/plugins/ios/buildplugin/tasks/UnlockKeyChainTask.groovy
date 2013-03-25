package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import org.gradle.api.Project

import static com.apphance.ameba.PropertyCategory.readOptionalPropertyOrEnvironmentVariable
import static org.gradle.api.logging.Logging.getLogger

class UnlockKeyChainTask {

    private l = getLogger(getClass())

    private Project project
    private CommandExecutor executor

    void unlockKeyChain() {
        def keychainPassword = readOptionalPropertyOrEnvironmentVariable(project, 'osx.keychain.password')
        def keychainLocation = readOptionalPropertyOrEnvironmentVariable(project, 'osx.keychain.location')
        if (keychainLocation != null && keychainPassword != null) {
            executor.executeCommand(new Command(runDir: project.rootDir, cmd: [
                    'security',
                    'unlock-keychain',
                    '-p',
                    keychainPassword,
                    keychainLocation]
            ))
        } else {
            l.warn("Seems that no keychain parameters are provided. Skipping unlocking the keychain.")
        }
    }
}
