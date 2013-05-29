package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.configuration.reader.PropertyReader
import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD
import static org.apache.commons.lang.StringUtils.isNotEmpty

class UnlockKeyChainTask extends DefaultTask {

    static final NAME = 'unlockKeyChain'
    String description = """|Unlocks key chain used during project building.
                            |Requires osx.keychain.password and osx.keychain.location properties
                            |or OSX_KEYCHAIN_PASSWORD and OSX_KEYCHAIN_LOCATION environment variable""".stripMargin()
    String group = AMEBA_BUILD

    @Inject CommandExecutor executor
    @Inject PropertyReader reader

    @TaskAction
    void unlockKeyChain() {
        def pass = reader.systemProperty('osx.keychain.password') ?: reader.envVariable('OSX_KEYCHAIN_PASSWORD') ?: null
        def location = reader.systemProperty('osx.keychain.location') ?: reader.envVariable('OSX_KEYCHAIN_PASSWORD') ?: null
        if (isNotEmpty(pass) && isNotEmpty(location)) {
            executor.executeCommand(new Command(runDir: project.rootDir, cmd: [
                    'security',
                    'unlock-keychain',
                    '-p',
                    pass,
                    location]
            ))
        } else {
            throw new GradleException("""|No keychain parameters provided. To unlock the keychain,
                                         |pass osx.keychain.password and osx.keychain.location
                                         |as java system properties (-D) or set OSX_KEYCHAIN_PASSWORD and
                                         |OSX_KEYCHAIN_LOCATION environment variables""".stripMargin())
        }
    }
}
