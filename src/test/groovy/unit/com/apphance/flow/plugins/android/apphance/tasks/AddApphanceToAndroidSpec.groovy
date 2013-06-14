package com.apphance.flow.plugins.android.apphance.tasks

import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.apphance.ApphanceMode
import com.google.common.io.Files
import org.apache.commons.io.FileUtils
import spock.lang.Specification

import static org.apache.commons.io.FileUtils.copyFile

class AddApphanceToAndroidSpec extends Specification {

    def androidVariantConf = new AndroidVariantConfiguration('test variant')
    def variantDir = Files.createTempDir()
    AddApphanceToAndroid addApphanceToAndroid

    def setup() {
        variantDir.deleteOnExit()
        FileUtils.copyDirectory(new File('testProjects/android/android-basic'), variantDir)

        androidVariantConf.apphanceMode.value = ApphanceMode.QA
        androidVariantConf.apphanceAppKey.value = 'TestKey'
        androidVariantConf.variantDir.value = variantDir

        addApphanceToAndroid = new AddApphanceToAndroid(androidVariantConf)
    }

    def 'test checkIfApphancePresent no apphance'() {
        expect:
        !addApphanceToAndroid.checkIfApphancePresent()
    }

    def 'test checkIfApphancePresent startNewSession'() {
        given:
        copyFile(new File('src/test/resources/com/apphance/flow/android/TestActivity.java.txt'), new File(variantDir,
                'src/com/apphance/flowTest/android/TestActivity.java'))

        expect:
        addApphanceToAndroid.checkIfApphancePresent()
    }

    def 'test checkIfApphancePresent apphance jar'() {
        given:
        new File(variantDir, 'libs/apphance-library.jar').createNewFile()

        expect:
        addApphanceToAndroid.checkIfApphancePresent()
    }

    def 'test checkIfApphancePresent apphance activity'() {
        given:
        copyFile(new File('src/test/resources/com/apphance/flow/android/AndroidManifestWithProblemActivity.xml'), new File(variantDir, 'AndroidManifest.xml'))

        expect:
        addApphanceToAndroid.checkIfApphancePresent()
    }

}
