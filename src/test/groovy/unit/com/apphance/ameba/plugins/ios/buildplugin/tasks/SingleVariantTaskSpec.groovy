package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant
import com.apphance.ameba.plugins.ios.buildplugin.IOSSingleVariantBuilder
import spock.lang.Specification
import spock.lang.Unroll

import static org.gradle.testfixtures.ProjectBuilder.builder

class SingleVariantTaskSpec extends Specification {

    @Unroll
    def 'builds #variant single variant'() {
        given:
        def proj = builder().build()

        and:
        def builder = GroovyMock(IOSSingleVariantBuilder)

        and:
        def task = proj.task('buildVariant1', type: SingleVariantTask) as SingleVariantTask
        task.builder = builder
        task.variant = variant

        when:
        task.buildSingleVariant()

        then:
        invocations * builder.buildVariant(variant)

        where:
        variant                        | invocations
        GroovyMock(AbstractIOSVariant) | 1
        null                           | 0

    }
}
