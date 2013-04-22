package com.apphance.ameba.configuration

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.properties.AbstractProperty
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

class ConfigurationSpec extends Specification {

    @Shared
    def androidConf = new AndroidConfiguration(ProjectBuilder.builder().build(), * [null] * 5)

    def 'return list of fields annotated with @AmebaProp'() {
        when:
        def fields = androidConf.propertyFields

        then:
        fields.size() > 0
        fields.every { f -> (f.accessible = true) && f.get(androidConf).class.superclass == AbstractProperty }
    }

    def 'return list of properties'() {
        when:
        def props = androidConf.amebaProperties
        then:
        props*.name.containsAll(['android.project.name'])
    }

    def 'configuration name'() {
        expect: new AndroidReleaseConfiguration(* [null] * 2).enabledPropKey == 'android.release.configuration.enabled'
    }

    @Ignore('Rewrite access checking to Guice AOP')
    def "don't intercept non-ameba properties"() {
        given:
        def configuration = new AndroidReleaseConfiguration()
        configuration.enabled = false

        expect:
        configuration.locale == null
    }

    @Ignore('Rewrite access checking to Guice AOP')
    def "don't intercept non-ameba getters"() {
        given:
        def configuration = new AndroidReleaseConfiguration()
        configuration.enabled = false

        expect:
        configuration.getLocale() == null
        configuration.getBuildDate() == null
    }

    @Ignore('Rewrite access checking to Guice AOP')
    def 'throw exception when disabled and accessing property'() {
        given:
        def configuration = new AndroidReleaseConfiguration()
        configuration.enabled = false

        when:
        configuration.iconFile

        then:
        def e = thrown(IllegalStateException)
        e.message == AbstractConfiguration.ACCESS_DENIED
    }

    @Ignore('Rewrite access checking to Guice AOP')
    def 'throw exception when disabled and using getter'() {
        given:
        def configuration = new AndroidReleaseConfiguration()
        configuration.enabled = false

        when:
        configuration.getIconFile()

        then:
        thrown(IllegalStateException)
    }

    @Ignore('Rewrite access checking to Guice AOP')
    def 'no exception when enabled'() {
        given:
        def androidConfiguration = Spy(AndroidConfiguration)
        androidConfiguration.isEnabled() >> true
        def configuration = new AndroidReleaseConfiguration(androidConfiguration, null)
        configuration.enabled = true

        expect:
        androidConfiguration.isEnabled()
    }
}
