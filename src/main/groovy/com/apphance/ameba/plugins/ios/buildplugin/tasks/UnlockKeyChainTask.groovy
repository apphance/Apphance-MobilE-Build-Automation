package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.configuration.reader.PropertyReader
import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD
import static org.gradle.api.logging.Logging.getLogger

class UnlockKeyChainTask extends DefaultTask {

    static final NAME = 'unlockKeyChain'
    String description = """|Unlocks key chain used during project building.
                            |Requires osx.keychain.password and osx.keychain.location properties
                            |or OSX_KEYCHAIN_PASSWORD and OSX_KEYCHAIN_LOCATION environment variable""".stripMargin()
    String group = AMEBA_BUILD

    private l = getLogger(getClass())

    @Inject CommandExecutor executor
    @Inject PropertyReader reader

    @TaskAction
    void unlockKeyChain() {
        def pass = reader.systemProperty('osx.keychain.password') ?: reader.envVariable('OSX_KEYCHAIN_PASSWORD') ?: null
        def location = reader.systemProperty('osx.keychain.location') ?: reader.envVariable('OSX_KEYCHAIN_PASSWORD') ?: null
        if (location != null && pass != null) {
            executor.executeCommand(new Command(runDir: project.rootDir, cmd: [
                    'security',
                    'unlock-keychain',
                    '-p',
                    pass,
                    location]
            ))
        } else {
            l.warn("Seems that no keychain parameters are provided. Skipping unlocking the keychain.")
        }
    }
}
