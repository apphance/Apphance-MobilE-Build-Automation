package com.apphance.flow.configuration.ios.variants

import com.google.inject.AbstractModule
import com.google.inject.assistedinject.FactoryModuleBuilder
import spock.guice.UseModules
import spock.lang.Ignore
import spock.lang.Specification

import javax.inject.Inject

@UseModules(TestModule)
@Ignore('TODO')
class IOSWorkspaceVariantSpec extends Specification {

    class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            String s = 'lol'
            File b = new File('')
            b = s


            bind(IOSVariantsConfiguration).toInstance(GroovyMock(IOSVariantsConfiguration) {
                getWorkspaceXscheme() >> [['a', 'b'], ['a', 'c']]
            })
            install(new FactoryModuleBuilder().build(IOSVariantFactory))
        }
    }

    @Inject
    IOSVariantFactory variantFactory

    def 'scheme and workspace found for name'() {
        when:
        def variant = variantFactory.createWorkspaceVariant('ab')

        then:
        noExceptionThrown()
        variant.scheme == 'a'
        variant.workspace == 'b'
    }
}


