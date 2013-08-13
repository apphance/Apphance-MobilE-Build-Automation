package com.apphance.flow.plugins.ios.apphance

import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.configuration.properties.ApphanceModeProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.plugins.ios.parsers.PbxJsonParser
import spock.lang.Shared
import spock.lang.Specification

import static com.apphance.flow.configuration.apphance.ApphanceMode.*
import static com.google.common.io.Files.createTempDir
import static org.gradle.testfixtures.ProjectBuilder.builder

class IOSApphanceEnhancerSpec extends Specification {

    @Shared
    def tmpDir = createTempDir()

    def cleanup() {
        tmpDir.deleteDir()
    }

    def 'apphance framework is found as expected in variant dir'() {
        given:
        def variant = GroovyStub(IOSVariant) {
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
        def enhancer = new IOSApphanceEnhancer(GroovyMock(IOSVariant) {
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
        def enhancer = new IOSApphanceEnhancer(GroovyMock(IOSVariant) {
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

    def 'apphance lib dependency is constructed correctly'() {
        given:
        def enhancer = new IOSApphanceEnhancer(GroovyMock(IOSVariant) {
            getApphanceMode() >> new ApphanceModeProperty(value: apphanceMode)
            getApphanceLibVersion() >> new StringProperty(value: '1.8.2')
            getApphanceDependencyArch() >> 'armv7'
        })

        expect:
        enhancer.apphanceLibDependency == expectedDependency

        where:
        apphanceMode | expectedDependency
        QA           | 'com.apphance:ios.pre-production.armv7:1.8.2'
        SILENT       | 'com.apphance:ios.pre-production.armv7:1.8.2'
        PROD         | 'com.apphance:ios.production.armv7:1.8.2'
    }

    def 'framework folders are checked when exist'() {
        given:
        def tmpDir = createTempDir()

        def enhancer = new IOSApphanceEnhancer(GroovyMock(IOSVariant) {
            getTmpDir() >> tmpDir
            getApphanceMode() >> new ApphanceModeProperty(value: mode)
        })

        and:
        new File(tmpDir, folderName).mkdirs()

        when:
        enhancer.checkFrameworkFolders(dependency)

        then:
        noExceptionThrown()

        cleanup:
        tmpDir.deleteDir()

        where:
        dependency                                    | folderName                          | mode
        'com.apphance:ios.pre-production.armv7:1.8.2' | 'Apphance-Pre-Production.framework' | QA
        'com.apphance:ios.pre-production.armv7:1.8.2' | 'Apphance-Pre-Production.framework' | SILENT
        'com.apphance:ios.production.armv7:1.8.2'     | 'Apphance-Production.framework'     | PROD
    }

    def 'apphance configuration is added'() {
        given:
        def project = builder().build()

        expect:
        !project.configurations.asMap['apphance']
        !project.repositories

        when:
        new IOSApphanceEnhancer(null).addApphanceConfiguration(project, 'apphance')

        then:
        project.configurations.asMap['apphance']
        [
                'https://dev.polidea.pl/artifactory/libs-releases-local/',
                'https://dev.polidea.pl/artifactory/libs-snapshots-local/'
        ].sort() ==
                project.repositories*.url*.toString().sort()

    }
}
