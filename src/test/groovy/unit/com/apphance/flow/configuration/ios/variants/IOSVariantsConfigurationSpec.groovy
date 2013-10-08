package com.apphance.flow.configuration.ios.variants

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.reader.PropertyPersister
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.apphance.flow.plugins.ios.scheme.XCSchemeInfo
import com.apphance.flow.plugins.ios.workspace.XCWorkspaceLocator
import com.apphance.flow.util.FlowUtils
import org.apache.commons.io.FileUtils
import spock.lang.Specification

@Mixin(FlowUtils)
class IOSVariantsConfigurationSpec extends Specification {

    IOSConfiguration conf
    IOSVariantsConfiguration variantsConf

    def setup() {
        conf = GroovyMock(IOSConfiguration)
        variantsConf = new IOSVariantsConfiguration()
        variantsConf.conf = conf
        variantsConf.propertyPersister = Stub(PropertyPersister, { get(_) >> '' })
        variantsConf.variantFactory = GroovyMock(IOSVariantFactory) {
            createSchemeVariant(_) >> GroovyMock(IOSSchemeVariant) {
                isEnabled() >> true
            }
            createWorkspaceVariant(_) >> GroovyMock(IOSWorkspaceVariant) {
                isEnabled() >> true
            }
        }
    }

    def 'list of scheme variants is created'() {
        given:
        variantsConf.workspaceLocator = GroovyMock(XCWorkspaceLocator) {
            getHasWorkspaces() >> false
        }
        variantsConf.schemeInfo = GroovyMock(XCSchemeInfo) {
            getHasSchemes() >> true
        }
        variantsConf.variantsNames.value = ['v1', 'v2', 'v3']

        expect:
        variantsConf.variants.size() == 3
        variantsConf.variants.every { it.toString().contains(IOSSchemeVariant.class.simpleName) }
    }

    def 'list of workspace variants is created'() {
        given:
        variantsConf.workspaceLocator = GroovyMock(XCWorkspaceLocator) {
            getHasWorkspaces() >> true
        }
        variantsConf.schemeInfo = GroovyMock(XCSchemeInfo) {
            getHasSchemes() >> true
        }
        variantsConf.variantsNames.value = ['v1', 'v2', 'v3']

        expect:
        variantsConf.variants.size() == 3
        variantsConf.variants.every { it.toString().contains(IOSWorkspaceVariant.class.simpleName) }
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

    def 'possible scheme variants found'() {
        given:
        def tmpDir = temporaryDir
        FileUtils.copyDirectory(new File(XCSchemeInfo.getResource('iosProject').toURI()), tmpDir)

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
        def schemeInfo = new XCSchemeInfo(schemeParser: new XCSchemeParser(), conf: conf)

        and:
        variantsConf.conf = conf
        variantsConf.schemeInfo = schemeInfo

        and:
        variantsConf.workspaceLocator = GroovyMock(XCWorkspaceLocator) {
            getHasWorkspaces() >> false
        }

        expect:
        variantsConf.possibleVariants == (conf.schemes - ['GradleXCode 2', 'GradleXCodeNotShared'])
    }

    def 'possible workspace variants found'() {
        given:
        def tmpDir = temporaryDir
        FileUtils.copyDirectory(new File(XCSchemeInfo.getResource('iosProject').toURI()), tmpDir)

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
        def schemeInfo = new XCSchemeInfo(schemeParser: new XCSchemeParser(), conf: conf)

        and:
        variantsConf.conf = conf
        variantsConf.schemeInfo = schemeInfo

        and:
        variantsConf.workspaceLocator = GroovyMock(XCWorkspaceLocator) {
            getHasWorkspaces() >> true
            getWorkspaces() >> [new File('WS.xcworkspace')]
        }


        expect:
        variantsConf.possibleVariants == (conf.schemes - ['GradleXCode 2', 'GradleXCodeNotShared']).collect { "WS$it" }
    }
}
