package com.apphance.flow.configuration.variants

import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.configuration.ios.variants.IOSSchemeVariant
import com.apphance.flow.configuration.properties.ApphanceModeProperty
import com.apphance.flow.configuration.properties.IOSBuildModeProperty
import com.apphance.flow.detection.project.ProjectType
import com.apphance.flow.validation.PropertyValidator
import spock.lang.Specification

import static com.apphance.flow.configuration.apphance.ApphanceMode.PROD
import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE

class AbstractVariantSpec extends Specification {

    def 'test apphanceLibVersion validator'() {
        given:
        def variant = new AndroidVariantConfiguration('name')

        expect:
        variant.aphLib.validator(correct)

        where:
        correct << ['1.8', '1.8.2', '1.9-RC1', '2.0.1.1.1']
    }

    def 'apphance lib version validator handles incorrect value'() {
        given:
        def variant = new AndroidVariantConfiguration('name')

        expect:
        !variant.aphLib.validator(incorrect)

        where:
        incorrect << ['1.8RC', '1.9RC1', 'ver2', 'V', '1.9-RC1-M2', '1.9-RC1-RC2', '1.9--RC1', 'RC1']
    }

    def 'aph iOS properties interactive base on variant type'() {
        given:
        variant.apphanceConf = GroovyStub(ApphanceConfiguration) { isEnabled() >> true }
        variant.aphMode = new ApphanceModeProperty(value: PROD)
        if (variant.projectType == ProjectType.IOS) {
            variant.mode = new IOSBuildModeProperty(value: DEVICE)
        }

        expect:
        variant.aphReportOnDoubleSlide.interactive() == (variant.projectType == ProjectType.IOS)
        variant.aphMachException.interactive() == (variant.projectType == ProjectType.IOS)
        variant.aphAppVersionCode.interactive() == (variant.projectType == ProjectType.IOS)
        variant.aphAppVersionName.interactive() == (variant.projectType == ProjectType.IOS)

        where:
        variant << [new AndroidVariantConfiguration(''), new IOSSchemeVariant('')]
    }

    def 'aph lib url is validated well'() {
        given:
        def variant = new IOSSchemeVariant('')
        variant.apphanceConf = GroovyStub(ApphanceConfiguration) { isEnabled() >> true }
        variant.aphMode = new ApphanceModeProperty(value: PROD)
        if (variant.projectType == ProjectType.IOS) {
            variant.mode = new IOSBuildModeProperty(value: DEVICE)
        }
        variant.propValidator = new PropertyValidator()

        expect:
        variant.aphLibURL.validator(url) == expected

        where:
        url               | expected
        null              | true
        ''                | true
        'bolo'            | false
        'http://some.com' | true

    }
}
