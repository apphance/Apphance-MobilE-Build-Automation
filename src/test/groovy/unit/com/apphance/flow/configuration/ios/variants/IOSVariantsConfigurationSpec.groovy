package com.apphance.flow.configuration.ios.variants

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.reader.PropertyPersister
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.apphance.flow.util.FlowUtils
import org.apache.commons.io.FileUtils
import spock.lang.Specification

@Mixin(FlowUtils)
class IOSVariantsConfigurationSpec extends Specification {

    private IOSConfiguration conf
    private IOSVariantsConfiguration variantsConf

    def setup() {
        conf = GroovyMock(IOSConfiguration)
        def vf = GroovyMock(IOSVariantFactory)
        vf.createSchemeVariant(_) >> GroovyMock(IOSVariant) {
            isEnabled() >> true
        }

        variantsConf = new IOSVariantsConfiguration()
        variantsConf.conf = conf
        variantsConf.propertyPersister = Stub(PropertyPersister, { get(_) >> '' })
        variantsConf.variantFactory = vf
    }

    def 'test buildVariantsList variant'() {
        given:
        conf.schemes >> ['v1', 'v2', 'v3']
        variantsConf.variantsNames.value = ['v1', 'v2', 'v3']

        expect:
        variantsConf.variants.size() == 3
    }

    def 'variantNames validator works'() {
        given:
        def variantsConf = GroovySpy(IOSVariantsConfiguration)
        variantsConf.possibleVariants >> ['v1', 'v2']

        expect:
        variantsConf.variantsNames.validator(input) == expected

        where:
        input        | expected
        ['v1', 'v1'] | false
        '[v1,v1]'    | false
        '[v1,v2]'    | true
        ['v1', 'v2'] | true
        []           | false
        '[]'         | false
        ['\n']       | false
    }

    def 'possible variants found'() {
        given:
        def tmpDir = temporaryDir
        FileUtils.copyDirectory(new File(getClass().getResource('iosProject').toURI()), tmpDir)

        and:
        def conf = GroovyMock(IOSConfiguration) {
            getRootDir() >> tmpDir
            getSchemes() >> ['GradleXCode',
                    'GradleXCode With Space',
                    'GradleXCodeNoLaunchAction',
                    'GradleXCodeWithApphance',
                    'GradleXCodeWith2Targets',
                    'GradleXCode 2',
                    'GradleXCodeNotShared']
        }

        and:
        def schemeInfo = new IOSSchemeInfo(schemeParser: new XCSchemeParser(), conf: conf)

        and:
        variantsConf.conf = conf
        variantsConf.schemeInfo = schemeInfo

        expect:
        variantsConf.possibleVariants == (conf.schemes - ['GradleXCode 2', 'GradleXCodeNotShared'])
    }
}
