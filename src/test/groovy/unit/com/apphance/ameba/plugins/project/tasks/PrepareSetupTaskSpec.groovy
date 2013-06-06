package com.apphance.ameba.plugins.project.tasks

import com.apphance.ameba.TestUtils
import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.reader.ConfigurationWizard
import com.apphance.ameba.configuration.reader.PropertyPersister
import spock.lang.Specification
import spock.lang.Unroll

@Mixin(TestUtils)
class PrepareSetupTaskSpec extends Specification {

    def task = create PrepareSetupTask
    AbstractConfiguration conf
    def configurationWizard = GroovyMock(ConfigurationWizard)

    def setup() {
        conf = new AndroidConfiguration()
        task.configurations = [(1): conf]
        task.configurationWizard = configurationWizard
        task.propertyPersister = Mock(PropertyPersister)
    }

    @Unroll
    def 'interactive mode is correctly determined from system properties. #props, #mode'() {
        when:
        System.clearProperty('ni')
        System.clearProperty('noninteractive')
        props.each { String key, String val ->
            System.setProperty(key, val)
        }
        task.prepareSetup()

        then:
        1 * configurationWizard.setInteractiveMode(mode)

        where:
        props                        | mode
        [abc: 'abc']                 | true
        [non: '']                    | true

        [ni: '']                     | false
        [ni: 'false']                | false
        [ni: 'true']                 | false
        [ni: 'anything']             | false
        [noninteractive: '']         | false
        [noninteractive: 'false']    | false
        [noninteractive: 'true']     | false
        [noninteractive: 'anything'] | false

    }

    def 'configurations are resolved and persisted'() {
        when:
        task.prepareSetup()

        then:
        1 * configurationWizard.resolveConfigurations(_)
        1 * task.propertyPersister.save(_)
    }
}
