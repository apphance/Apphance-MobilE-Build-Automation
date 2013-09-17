package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.configuration.properties.IOSBuildModeProperty
import com.apphance.flow.plugins.ios.parsers.MobileProvisionParser
import org.gradle.api.GradleException
import spock.lang.Specification

import static com.apphance.flow.configuration.ios.IOSBuildMode.*
import static org.gradle.testfixtures.ProjectBuilder.builder

class CopyMobileProvisionTaskSpec extends Specification {

    def projectDir = 'testProjects/ios/GradleXCode'
    def project = builder().withProjectDir(new File(projectDir)).build()
    def mobileprovisionFile = new File(projectDir, 'release/distribution_resources/GradleXCode.mobileprovision')
    def mobileProvisionDir = new File("${System.getProperty('user.home')}/Library/MobileDevice/Provisioning Profiles/")

    def task = project.task(CopyMobileProvisionTask.NAME, type: CopyMobileProvisionTask) as CopyMobileProvisionTask

    def setup() {
        task.mpParser = GroovyStub(MobileProvisionParser, {
            bundleId(mobileprovisionFile) >> 'MT2B94Q7N6.com.apphance.flow'
        })
    }

    def 'files are copied with no exceptions when bundleId match'() {
        given:
        def v1 = GroovyMock(AbstractIOSVariant) { getMode() >> new IOSBuildModeProperty(value: DEVICE) }
        def v2 = GroovyMock(AbstractIOSVariant) { getMode() >> new IOSBuildModeProperty(value: FRAMEWORK) }
        def v3 = GroovyMock(AbstractIOSVariant) { getMode() >> new IOSBuildModeProperty(value: SIMULATOR) }
        task.variantsConf = GroovyStub(IOSVariantsConfiguration, { getVariants() >> [v1, v2, v3] })

        and:
        def copiedProfile = new File(mobileProvisionDir, mobileprovisionFile.name)
        copiedProfile.delete()

        expect:
        !copiedProfile.exists()

        when:
        task.copyMobileProvision()

        then:
        noExceptionThrown()

        and:
        copiedProfile.exists()
        copiedProfile.size() > 0

        and:
        1 * v1.getMobileprovision() >> new FileProperty(value: mobileprovisionFile)
        1 * v1.getBundleId() >> 'MT2B94Q7N6.com.apphance.flow'
        0 * v2.getMobileprovision()
        0 * v2.getBundleId()
        0 * v3.getMobileprovision()
        0 * v3.getBundleId()

        cleanup:
        copiedProfile?.delete()
    }

    def 'exception is thrown when mobileprovision is copied and bundle id does not match'() {
        given:
        task.variantsConf = GroovyStub(IOSVariantsConfiguration, {
            getVariants() >> [
                    GroovyStub(AbstractIOSVariant) {
                        getName() >> 'SampleVariant'
                        getMobileprovision() >> new FileProperty(value: mobileprovisionFile)
                        getBundleId() >> 'MT2B94Q7N6.com.apphance.flowa'
                        getMode() >> new IOSBuildModeProperty(value: DEVICE)
                    }
            ]
        })

        when:
        task.copyMobileProvision()

        then:
        def e = thrown(GradleException)
        e.message.startsWith('Bundle Id from variant: SampleVariant (MT2B94Q7N6.com.apphance.flowa)')
        e.message.contains('(MT2B94Q7N6.com.apphance.flowa)')
        e.message.contains('(MT2B94Q7N6.com.apphance.flow)')
        e.message.contains('GradleXCode.mobileprovision')
    }
}
