package com.apphance.flow.plugins.ios.apphance

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.properties.ApphanceModeProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.plugins.ios.parsers.PbxJsonParser
import spock.lang.Shared
import spock.lang.Specification

import static com.apphance.flow.configuration.apphance.ApphanceMode.*
import static com.google.common.io.Files.createTempDir

class IOSApphanceEnhancerSpec extends Specification {

    @Shared
    def tmpDir = createTempDir()

    def cleanup() {
        tmpDir.deleteDir()
    }

    def 'apphance framework is found as expected in variant dir'() {
        given:
        def variant = GroovyStub(AbstractIOSVariant) {
            getTmpDir() >> variantDir
        }

        and:
        def enhancer = new IOSApphanceEnhancer(variant)

        when:
        modify.call()

        then:
        found == enhancer.findApphanceInPath()

        where:
        found | variantDir | modify
        false | tmpDir     | {}
        true  | tmpDir     | { new File(tmpDir, 'Apphance.framework').mkdirs() }
        false | tmpDir     | { new File(tmpDir, 'Apph2ance.framework').mkdirs() }
        true  | tmpDir     | { new File(tmpDir, 'apphance.framework').mkdirs() }
        true  | tmpDir     | { new File(tmpDir, 'Apphance-Production.framework').mkdirs() }
        true  | tmpDir     | { new File(tmpDir, 'Apphance-Pre-Production.framework').mkdirs() }
        false | tmpDir     | { new File(tmpDir, 'Apphance.framewoork').mkdirs() }
    }

    def 'no exception is thrown when apphance found in variant dir'() {
        given:
        def enhancer = new IOSApphanceEnhancer(GroovyMock(AbstractIOSVariant) {
            getName() >> 'Variant1'
            getTmpDir() >> new File('variant1/dir')
        })

        and:
        enhancer.pbxJsonParser = GroovyMock(PbxJsonParser) {
            isFrameworkDeclared(_, _) >> true
        }

        when:
        enhancer.enhanceApphance()

        then:
        noExceptionThrown()
    }

    def 'apphance dependency group resolved'() {
        given:
        def enhancer = new IOSApphanceEnhancer(GroovyMock(AbstractIOSVariant) {
            getApphanceMode() >> new ApphanceModeProperty(value: apphanceMode)
        })

        expect:
        enhancer.apphanceDependencyGroup == expectedDependency

        where:
        apphanceMode | expectedDependency
        QA           | 'pre-production'
        SILENT       | 'pre-production'
        PROD         | 'production'
    }

    def 'apphance zip url is constructed correctly'() {
        given:
        def enhancer = new IOSApphanceEnhancer(GroovyMock(AbstractIOSVariant) {
            getApphanceMode() >> new ApphanceModeProperty(value: apphanceMode)
            getApphanceLibVersion() >> new StringProperty(value: '1.8.8')
            getApphanceDependencyArch() >> 'armv7'
        })

        expect:
        enhancer.apphanceUrl == expectedUrl

        where:
        apphanceMode | expectedUrl
        QA           | 'https://dev.polidea.pl/artifactory/libs-releases-local/com/utest/apphance-preprod/1.8.8/apphance-preprod-1.8.8-armv7.zip'
        SILENT       | 'https://dev.polidea.pl/artifactory/libs-releases-local/com/utest/apphance-preprod/1.8.8/apphance-preprod-1.8.8-armv7.zip'
        PROD         | 'https://dev.polidea.pl/artifactory/libs-releases-local/com/utest/apphance-prod/1.8.8/apphance-prod-1.8.8-armv7.zip'
    }

    def 'framework folders are checked when exist'() {
        given:
        def tmpDir = createTempDir()

        def enhancer = new IOSApphanceEnhancer(GroovyMock(AbstractIOSVariant) {
            getTmpDir() >> tmpDir
            getApphanceMode() >> new ApphanceModeProperty(value: mode)
            getApphanceLibVersion() >> new StringProperty(value: '1.8.')
        })

        and:
        new File(tmpDir, folderName).mkdirs()

        when:
        enhancer.checkFrameworkFolders()

        then:
        noExceptionThrown()

        cleanup:
        tmpDir.deleteDir()

        where:
        folderName                          | mode
        'Apphance-Pre-Production.framework' | QA
        'Apphance-Pre-Production.framework' | SILENT
        'Apphance-Production.framework'     | PROD
    }
}
