package com.apphance.flow.plugins.android.apphance.tasks

import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.apphance.ApphanceMode
import com.google.common.io.Files
import org.apache.commons.io.FileUtils
import spock.lang.Specification

class AddApphanceToAndroidIntegrationTest extends Specification {

    def variantDir = Files.createTempDir()
    AddApphanceToAndroid addApphanceToAndroid

    def setup() {
        variantDir.deleteOnExit()
        FileUtils.copyDirectory(new File('testProjects/android/android-basic'), variantDir)

        def androidVariantConf = new AndroidVariantConfiguration('test variant')
        androidVariantConf.apphanceMode.value = ApphanceMode.QA
        androidVariantConf.apphanceAppKey.value = 'TestKey'
        androidVariantConf.variantDir.value = variantDir

        addApphanceToAndroid = new AddApphanceToAndroid(androidVariantConf)
    }

    def 'test add apphance lib'() {
        expect:
        !addApphanceToAndroid.checkIfApphancePresent()
        def version = '1.9-RC1'

        when:
        addApphanceToAndroid.addApphanceLib()

        then:
        addApphanceToAndroid.checkIfApphancePresent()
        new File(variantDir, "/libs/apphance-library-${version}/libs/apphance-library-${version}.jar").exists()
    }
}
