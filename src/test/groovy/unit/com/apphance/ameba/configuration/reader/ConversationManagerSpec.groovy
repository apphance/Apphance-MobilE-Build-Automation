package com.apphance.ameba.configuration.reader

import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.LongProperty
import com.apphance.ameba.configuration.properties.ProjectTypeProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.detection.ProjectType
import spock.lang.Specification

import javax.imageio.ImageIO

import static com.apphance.ameba.detection.ProjectType.ANDROID
import static java.lang.System.getProperties

class ConversationManagerSpec extends Specification {

    def cm = new ConversationManager()

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
        cm.defaultValueString(p) == expectedString

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
        new StringProperty(message: 'Project name', defaultValue: { 'a' }, possibleValues: { ['a', 'b'] as List<String> }) | "Project name, default: 'a', possible: [a, b]: "
        new StringProperty(message: 'Project name', possibleValues: { ['a', 'b'] as List<String> })                        | "Project name, default: '', possible: [a, b]: "
    }

    def 'input validation works well'() {
        expect:
        cm.validateInput(input, p) == validationResult

        where:
        p                                                                                                                   | input  | validationResult
        new StringProperty(validator: { false })                                                                            | ''     | true
        new StringProperty(validator: { false })                                                                            | '\n '  | true
        new StringProperty(validator: { false })                                                                            | 'v1'   | false
        new StringProperty(possibleValues: { ['v1', 'v2'] as List<String> }, validator: { false })                          | 'v1'   | true
        new StringProperty(possibleValues: { ['v1', 'v2'] as List<String> }, validator: { false })                          | 'v3'   | false
        new StringProperty(possibleValues: { ['a', 'b'] as List<String> }, validator: { it.matches('[0-9]+') })             | '1234' | true
        new StringProperty(possibleValues: { ['a', 'b'] as List<String> }, validator: { it.matches('[0-9]+') })             | 'a'    | true
        new StringProperty(possibleValues: { ['a', 'b'] as List<String> }, validator: { it.matches('[0-9]+') })             | 'c'    | false
        new FileProperty(validator: { it?.trim() ? new File(it as String).exists() && ImageIO.read(new File(it)) : false }) | 'bolo' | false
    }

    def 'property value is set well'() {
        when:
        cm.setPropertyValue(p, input)

        then:
        p.hashCode()
        p.value == expectedValue

        where:
        p                                                      | input                                   | expectedValue
        new StringProperty(defaultValue: { 'lol' })            | ''                                      | 'lol'
        new StringProperty()                                   | ''                                      | ''
        new LongProperty(validator: { it.matches('[0-9]') })   | '5'                                     | 5L
        new FileProperty(validator: { new File(it).exists() }) | properties['java.io.tmpdir'].toString() | new File(properties['java.io.tmpdir'].toString())
    }
}
