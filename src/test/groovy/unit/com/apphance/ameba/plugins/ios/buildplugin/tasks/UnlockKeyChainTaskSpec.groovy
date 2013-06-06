package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.TestUtils
import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.configuration.reader.PropertyReader
import com.apphance.ameba.executor.command.CommandExecutor
import org.gradle.api.GradleException
import spock.lang.Specification

@Mixin(TestUtils)
class UnlockKeyChainTaskSpec extends Specification {

    def task = create(UnlockKeyChainTask)

    def setup() {
        task.conf = GroovyStub(ProjectConfiguration)
    }

    def 'exception is thrown when no keychain properties set'() {
        given:
        task.reader = new PropertyReader()

        when:
        task.unlockKeyChain()

        then:
        def e = thrown(GradleException)
        e.message == """|No keychain parameters provided. To unlock the keychain,
                                         |pass osx.keychain.password and osx.keychain.location
                                         |as java system properties (-D) or set OSX_KEYCHAIN_PASSWORD and
                                         |OSX_KEYCHAIN_LOCATION environment variables""".stripMargin()
    }

    def 'keychain is unlocked when system properties passed'() {
        given:
        System.setProperty('osx.keychain.location', 'loc')
        System.setProperty('osx.keychain.password', 'pass')

        and:
        def executor = GroovyMock(CommandExecutor)

        and:
        task.reader = new PropertyReader()
        task.executor = executor

        when:
        task.unlockKeyChain()

        then:
        1 * executor.executeCommand({ it.commandForExecution == ['security', 'unlock-keychain', '-p', 'pass', 'loc'] })

        cleanup:
        System.properties.remove('osx.keychain.location')
        System.properties.remove('osx.keychain.password')
    }
}
