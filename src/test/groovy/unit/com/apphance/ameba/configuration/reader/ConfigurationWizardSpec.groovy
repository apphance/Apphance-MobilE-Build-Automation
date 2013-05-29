package com.apphance.ameba.configuration.reader

import com.apphance.ameba.configuration.properties.ProjectTypeProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.detection.ProjectType
import spock.lang.Specification

import static com.apphance.ameba.detection.ProjectType.ANDROID

class ConfigurationWizardSpec extends Specification {

    def cm = new ConfigurationWizard()

    def 'possible value string is formatted correctly'() {
        expect:
        cm.possibleValuesString(p) == expectedString

        where:
        p                                                                                                | expectedString
        new StringProperty()                                                                             | ''
        new StringProperty(possibleValues: { ['a', 'b'] as List<String> })                               | ', possible: [a, b]'
        new ProjectTypeProperty(possibleValues: { ProjectType.values()*.name().sort() as List<String> }) | ', possible: [ANDROID, IOS]'
    }

    def 'default value string is formatted correctly'() {
        expect:
        cm.effectiveDefaultValue(p) == expectedString

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
        cm.prompt(p) == expectedString

        where:
        p                                                                                                                  | expectedString
        new StringProperty(message: 'Project name')                                                                        | "Project name, default: '': "
        new StringProperty(message: 'Project name', defaultValue: { 'a' })                                                 | "Project name, default: 'a': "
        new StringProperty(message: 'Project name', defaultValue: { 'b' }, possibleValues: { ['a', 'b'] as List<String> }) | "Project name, default: 'b', " +
                "possible: [a, b]: "
        new StringProperty(message: 'Project name', possibleValues: { ['a', 'b'] as List<String> })                        | "Project name, default: 'a', " +
                "possible: [a, b]: "
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
}