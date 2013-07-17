package com.apphance.flow.configuration.reader

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.properties.ListStringProperty
import com.apphance.flow.configuration.properties.ProjectTypeProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.detection.project.ProjectType
import spock.lang.Specification

import static ProjectType.ANDROID
import static org.apache.commons.lang.StringUtils.isBlank

class ConfigurationWizardSpec extends Specification {

    def cm = new ConfigurationWizard()

    static String removeColor(String str) {
        str.replaceAll(/\033\[[0-9;]*m/, '')
    }

    def 'possible value string is formatted correctly'() {
        expect:
        removeColor(cm.promptPossible(p)) == expectedString

        where:
        p                                                                                                | expectedString
        new StringProperty()                                                                             | ''
        new StringProperty(possibleValues: { ['a', 'b'] as List<String> })                               | ', possible: [a, b]'
        new ProjectTypeProperty(possibleValues: { ProjectType.values()*.name().sort() as List<String> }) | ', possible: [ANDROID, IOS]'
    }

    def 'default value string is formatted correctly'() {
        expect:
        p.effectiveDefaultValue() == expectedString

        where:
        p                                                  | expectedString
        new StringProperty()                               | ''
        new StringProperty(defaultValue: { null })         | ''
        new StringProperty(defaultValue: { '' })           | ''
        new StringProperty(defaultValue: { 'a' })          | 'a'
        new ProjectTypeProperty(defaultValue: { ANDROID }) | 'ANDROID'
    }

    def 'prompt is displayed well'() {
        expect:
        removeColor(cm.prompt(p)) == expectedString

        where:
        p                                                                                                  | expectedString
        new StringProperty(message: 'Project name')                                                        | "Project name\n: "
        new StringProperty(message: 'Project name', defaultValue: { 'a' })                                 | "Project name\ndefault: 'a': "
        new StringProperty(message: 'Project name', defaultValue: { 'b' }, possibleValues: { ['a', 'b'] }) | "Project name, possible: [a, b]\ndefault: 'b': "
        new StringProperty(message: 'Project name', possibleValues: { ['a', 'b'] })                        | "Project name, possible: [a, b]\ndefault: 'a': "
    }

    def 'empty input validation works well.'() {
        expect:
        cm.validateInput(input, p) == validationResult

        where:
        p                                                                                  | input  | validationResult
        new StringProperty(value: 'val', defaultValue: { '' }, required: { false })        | '  '   | true
        new StringProperty(value: 'val', defaultValue: { 'def val' }, required: { false }) | '\n'   | true
        new StringProperty(value: 'val', defaultValue: { '' }, required: { true })         | '  '   | true
        new StringProperty(value: 'val', defaultValue: { 'def val' }, required: { true })  | null   | true
        new StringProperty(value: '', defaultValue: { 'def val' }, required: { true })     | '\t'   | true
        new StringProperty(value: '', defaultValue: { '' }, required: { true })            | '\n\n' | false
        new StringProperty(value: '', defaultValue: { '' }, required: { false })           | ''     | true
        new StringProperty(value: '', defaultValue: { 'def val' }, required: { false })    | '   '  | true
    }

    def 'input validation works well'() {
        expect:
        cm.validateInput(input, p) == validationResult

        where:
        p                                                                                   | input | validationResult
        new StringProperty()                                                                | 'aaa' | true
        new StringProperty(possibleValues: { ['aaa'] })                                     | 'aaa' | true
        new StringProperty(possibleValues: { ['bbb'] })                                     | 'aaa' | false
        new StringProperty(possibleValues: { ['bbb'] }, validator: { it.matches('[a]+') })  | 'aaa' | true
        new StringProperty(possibleValues: { ['bbb'] }, validator: { !it.matches('[a]+') }) | 'aaa' | false
        new StringProperty(validator: { it.matches('[a]+') })                               | 'aaa' | true
        new StringProperty(validator: { !it.matches('[a]+') }, possibleValues: { ['aaa'] }) | 'aaa' | false
        new StringProperty(validator: { !it.matches('[a]+') })                              | 'aaa' | false
    }

    def 'property value is set well'() {
        when:
        cm.setPropertyValue(p, input)

        then:
        p.value == expectedValue

        where:
        p                                                         | input | expectedValue
        new StringProperty()                                      | '\n'  | null
        new StringProperty(defaultValue: { 'aaa' })               | ''    | 'aaa'
        new StringProperty(value: 'aaa')                          | ''    | 'aaa'
        new StringProperty(value: 'bbb', defaultValue: { 'bbb' }) | 'aaa' | 'aaa'
    }

    def 'test interactivity dynamically changed'() {
        given:
        def conf = GroovyStub(AbstractConfiguration)
        StringProperty first = new StringProperty(value: '')
        ListStringProperty second = new ListStringProperty(interactive: { isBlank(first.value) })
        conf.propertyFields >> [first, second]
        def wizard = GroovySpy(ConfigurationWizard)
        wizard.readProperty(_) >> { first.value = 'abc' }

        when:
        wizard.readValues(conf)

        then:
        0 * wizard.readProperty(second)
    }

    def "don't call enablePlugin in non-interactive mode"() {
        given:
        def wizard = GroovySpy(ConfigurationWizard)
        wizard.enablePlugin(_) >> {}
        def conf = GroovyStub(AbstractConfiguration)
        conf.enabled >> false
        wizard.interactiveMode = mode

        when:

        wizard.resolveConfigurations([conf])

        then:
        calls * wizard.enablePlugin(_)

        where:
        mode  | calls
        true  | 1
        false | 0
    }

    def "don't call reader for input in interactive mode"() {
        given:
        def wizard = GroovySpy(ConfigurationWizard)
        wizard.reader = GroovyMock(Reader)
        wizard.interactiveMode = mode

        when:
        wizard.getInput()

        then:
        calls * wizard.reader.readLine()

        where:
        mode  | calls
        true  | 1
        false | 0
    }
}
