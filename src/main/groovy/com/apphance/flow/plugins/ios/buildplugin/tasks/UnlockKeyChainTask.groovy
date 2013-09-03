package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ProjectConfiguration
import com.apphance.flow.configuration.reader.PropertyReader
import com.apphance.flow.executor.command.Command
import com.apphance.flow.executor.command.CommandExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD
import static java.util.ResourceBundle.getBundle
import static org.apache.commons.lang.StringUtils.isNotEmpty

class UnlockKeyChainTask extends DefaultTask {

    static final NAME = 'unlockKeyChain'
    String description = "Unlocks key chain used during project building. Requires osx.keychain.password and " +
            "osx.keychain.location properties or OSX_KEYCHAIN_PASSWORD and OSX_KEYCHAIN_LOCATION environment variable"
    String group = FLOW_BUILD

    @Inject ProjectConfiguration conf
    @Inject CommandExecutor executor
    @Inject PropertyReader reader

    private bundle = getBundle('validation')

    @TaskAction
    void unlockKeyChain() {
        def pass = reader.systemProperty('osx.keychain.password') ?: reader.envVariable('OSX_KEYCHAIN_PASSWORD') ?: null
        def location = reader.systemProperty('osx.keychain.location') ?: reader.envVariable('OSX_KEYCHAIN_LOCATION') ?: null
        if (isNotEmpty(pass) && isNotEmpty(location)) {
            executor.executeCommand(new Command(
                    runDir: conf.rootDir,
                    cmd: ['security', 'unlock-keychain', '-p', '$pass', location],
                    secretParams: [pass: pass]
            ))
        } else
            throw new GradleException(bundle.getString('exception.ios.keychain'))
    }
}
