package com.apphance.ameba.plugins.ios

import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant
import com.apphance.ameba.configuration.properties.FileProperty
import spock.lang.Specification

import static java.lang.System.getProperties

class IOSArtifactProviderSpec extends Specification {

    def 'builder info is created correctly from variant'() {
        given:
        def variant = GroovyMock(AbstractIOSVariant)
        variant.target >> 'GradleXCode'
        variant.configuration >> 'BasicConfiguration'
        variant.tmpDir >> new File(properties['java.io.tmpdir'])
        variant.mobileprovision >> new FileProperty(value: new File('sample.mobileprovision'))
        variant.versionString >> '1.0.1'
        variant.versionCode >> '42'
        variant.fullVersionString >> '1.0.1_42'
        variant.plist >> new File('GradleXCode-Info.plist')

        and:
        def provider = new IOSArtifactProvider()

        when:
        def bi = provider.builderInfo(variant)

        then:
        bi.target == 'GradleXCode'
        bi.configuration == 'BasicConfiguration'
        bi.buildDir == new File(properties['java.io.tmpdir'], 'build')
        bi.filePrefix == 'GradleXCode-BasicConfiguration-1.0.1_42'
        bi.fullReleaseName == 'GradleXCode-BasicConfiguration-1.0.1_42'
        bi.id == 'GradleXCode-BasicConfiguration'
        bi.plistFile.name == 'GradleXCode-Info.plist'
        bi.mobileProvisionFile.name == 'sample.mobileprovision'
    }
}
