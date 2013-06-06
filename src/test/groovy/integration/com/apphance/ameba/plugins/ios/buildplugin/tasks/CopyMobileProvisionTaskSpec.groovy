package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant
import com.apphance.ameba.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.plugins.ios.parsers.MobileProvisionParser
import org.gradle.api.GradleException
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder

class CopyMobileProvisionTaskSpec extends Specification {

    def projectDir = 'testProjects/ios/GradleXCode'
    def project = builder().withProjectDir(new File(projectDir)).build()
    def mobileprovisionFile = new File(projectDir, 'release/distribution_resources/Ameba_Test_Project.mobileprovision')
    def mobileProvisionDir = new File("${System.getProperty('user.home')}/Library/MobileDevice/Provisioning Profiles/")

    def task = project.task(CopyMobileProvisionTask.NAME, type: CopyMobileProvisionTask) as CopyMobileProvisionTask

    def setup() {
        task.mpParser = GroovyStub(MobileProvisionParser, {
            bundleId(mobileprovisionFile) >> 'MT2B94Q7N6.com.apphance.ameba'
        })
    }

    def 'files are copied with no exceptions when bundleId match'() {
        given:
        task.variantsConf = GroovyStub(IOSVariantsConfiguration, {
            getVariants() >> [
                    GroovyStub(AbstractIOSVariant) {
                        getName() >> 'SampleVariant'
                        getMobileprovision() >> new FileProperty(value: mobileprovisionFile)
                        getEffectiveBundleId() >> 'MT2B94Q7N6.com.apphance.ameba'
                    }
            ]
        })

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
                        getEffectiveBundleId() >> 'MT2B94Q7N6.com.apphance.amebaa'
                    }
            ]
        })

        when:
        task.copyMobileProvision()

        then:
        def e = thrown(GradleException)
        println e.message
        e.message.startsWith('Bundle Id from variant: SampleVariant (MT2B94Q7N6.com.apphance.amebaa)')
        e.message.contains('(MT2B94Q7N6.com.apphance.amebaa)')
        e.message.contains('(MT2B94Q7N6.com.apphance.ameba)')
        e.message.contains('Ameba_Test_Project.mobileprovision')

    }
}
