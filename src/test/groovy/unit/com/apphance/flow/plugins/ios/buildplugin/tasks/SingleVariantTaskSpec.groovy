package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.plugins.ios.buildplugin.IOSSingleVariantBuilder
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
        def task = proj.task('buildVariant1', type: BuildVariantTask) as BuildVariantTask
        task.builder = builder
        task.variant = variant

        when:
        task.buildVariant()

        then:
        invocations * builder.buildVariant(variant)

        where:
        variant                        | invocations
        GroovyMock(AbstractIOSVariant) | 1
        null                           | 0

    }
}
