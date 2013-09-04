package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.TestUtils
import com.apphance.flow.configuration.ProjectConfiguration
import com.apphance.flow.configuration.reader.PropertyReader
import com.apphance.flow.executor.command.CommandExecutor
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
        def reader = GroovyMock(PropertyReader)
        reader.envVariable(_) >> null
        reader.systemProperty(_) >> null
        task.reader = reader

        when:
        task.unlockKeyChain()

        then:
        def e = thrown(GradleException)
        e.message == "No keychain parameters provided. To unlock the keychain, pass osx.keychain.password and osx.keychain.location as java system properties (-D) or set OSX_KEYCHAIN_PASSWORD and OSX_KEYCHAIN_LOCATION environment variables"
    }

    def 'keychain is unlocked when system properties passed'() {
        given:
        def reader = GroovyMock(PropertyReader)
        reader.systemProperty('osx.keychain.location') >> 'loc'
        reader.systemProperty('osx.keychain.password') >> 'pass'

        and:
        def executor = GroovyMock(CommandExecutor)

        and:
        task.reader = reader
        task.executor = executor

        when:
        task.unlockKeyChain()

        then:
        1 * executor.executeCommand({ it.commandForExecution == ['security', 'unlock-keychain', '-p', 'pass', 'loc'] })
    }

    def 'keychain is unlocked when env variables passed'() {
        given:
        def reader = GroovyMock(PropertyReader)
        reader.envVariable('OSX_KEYCHAIN_LOCATION') >> 'loc'
        reader.envVariable('OSX_KEYCHAIN_PASSWORD') >> 'pass'

        and:
        def executor = GroovyMock(CommandExecutor)

        and:
        task.reader = reader
        task.executor = executor

        when:
        task.unlockKeyChain()

        then:
        1 * executor.executeCommand({ it.commandForExecution == ['security', 'unlock-keychain', '-p', 'pass', 'loc'] })
    }

}
