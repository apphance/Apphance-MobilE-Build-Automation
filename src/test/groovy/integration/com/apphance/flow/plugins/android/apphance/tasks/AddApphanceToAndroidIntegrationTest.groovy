package com.apphance.flow.plugins.android.apphance.tasks

import com.apphance.flow.TestUtils
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import org.apache.commons.io.FileUtils
import spock.lang.Specification

import static com.apphance.flow.configuration.apphance.ApphanceMode.PROD
import static com.apphance.flow.configuration.apphance.ApphanceMode.QA

@Mixin(TestUtils)
class AddApphanceToAndroidIntegrationTest extends Specification {

    def variantDir
    AddApphanceToAndroid addApphanceToAndroid
    private AndroidVariantConfiguration androidVariantConf
    static def version = '1.9'

    def setup() {
        variantDir = temporaryDir
        FileUtils.copyDirectory(new File('projects/test/android/android-basic'), variantDir)

        androidVariantConf = GroovySpy(AndroidVariantConfiguration, constructorArgs: ['test variant'])
        androidVariantConf.aphAppKey.value = 'TestKey'
        androidVariantConf.aphLib.value = '1.9'
        androidVariantConf.getTmpDir() >> variantDir
    }

    def 'test add apphance lib'() {
        given:
        androidVariantConf.aphMode.value = mode
        addApphanceToAndroid = new AddApphanceToAndroid(androidVariantConf)

        expect:
        !addApphanceToAndroid.checkIfApphancePresent()

        when:
        addApphanceToAndroid.addApphanceLib()

        then:
        addApphanceToAndroid.checkIfApphancePresent()
        expectedFiles.each {
            new File(variantDir, "/libs/$it").exists()
        }

        (mode == PROD) ^ new File(variantDir, 'project.properties').text.contains("libs/apphance-library-${version}")

        where:
        mode | expectedFiles
        QA   | ["apphance-library-${version}/libs/apphance-library-${version}.jar", "apphance-library-${version}/libs/AndroidManifest.xml"]
        PROD | ["apphance-prod-${version}.jar"]
    }
}

