package com.apphance.ameba

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import static com.apphance.ameba.PropertyCategorySpec.SampleEnum.SAMPLE_PROPERTY

public class PropertyCategorySpec extends Specification {

    //TODO rewrite to use Mock after PropertyCategory refactor
    Project project

    def setup() {
        def pb = ProjectBuilder.builder()
        project = pb.build()
    }

    def 'reads property'() {
        given:
        project.ext['testProperty'] = '1000'

        expect:
        '1000' == PropertyCategory.readProperty(project, 'testProperty')
    }

    def 'reads property with default value'() {
        expect:
        '200' == PropertyCategory.readProperty(project, 'testProperty2', '200')
    }

    def 'null obtained when missing property read'() {
        expect:
        null == PropertyCategory.readProperty(project, 'notSuchProperty')
    }

    def 'reads property under enum key'() {
        given:
        def value = 'aaaa'
        project[SAMPLE_PROPERTY.propertyName] = value

        expect:
        value == PropertyCategory.readProperty(project, SAMPLE_PROPERTY)
    }

    def enum SampleEnum {
        SAMPLE_PROPERTY('samplePropName', 'samplePropValue')

        String propertyName
        String defaultValue

        SampleEnum(String propertyName, String defaultValue) {
            this.propertyName = propertyName
            this.defaultValue = defaultValue
        }
    }
}
