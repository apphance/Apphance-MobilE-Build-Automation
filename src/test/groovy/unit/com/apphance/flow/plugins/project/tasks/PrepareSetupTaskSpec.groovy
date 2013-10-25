package com.apphance.flow.plugins.project.tasks

import com.apphance.flow.TestUtils
import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.reader.ConfigurationWizard
import com.apphance.flow.configuration.reader.PropertyPersister
import spock.lang.Specification

import static org.apache.commons.collections.CollectionUtils.isEqualCollection

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

    def 'interactive mode is correctly determined from system properties'() {
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
        given:
        def confs = task.configurations.sort().values()

        when:
        task.prepareSetup()

        then:
        1 * configurationWizard.resolveConfigurations({ isEqualCollection(it, confs) })
        1 * task.propertyPersister.save({ isEqualCollection(it, confs) })
    }
}
