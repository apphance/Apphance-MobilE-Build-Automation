package com.apphance.flow.plugins.android.apphance.tasks

import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.apphance.ApphanceMode
import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper
import com.apphance.flow.util.FlowUtils
import org.gradle.api.GradleException

import static com.google.common.base.Preconditions.checkArgument
import static com.google.common.base.Preconditions.checkNotNull

@Mixin([FlowUtils, AndroidManifestHelper])
class AddApphanceToAndroid {

    final File variantDir
    final String apphanceAppKey
    final ApphanceMode apphanceMode

    AddApphanceToAndroid(AndroidVariantConfiguration androidVariantConf) {
        variantDir = androidVariantConf.variantDir.value
        apphanceAppKey = androidVariantConf.apphanceAppKey.value
        apphanceMode = androidVariantConf.apphanceMode.value
        checkArgument(variantDir.exists())
        checkNotNull(apphanceAppKey)
        checkNotNull(apphanceMode)
    }

    public void addApphance() {
        if (checkIfApphancePresent()) {
            throw new GradleException("Apphance was already added")
        }

        // Add Report Editor activity to the manifest
        // Add required permissions to the manifest
        // Add 'Apphance.startNewSession(...)' call to the main activity
        // Add 'Apphance.setCurrentActitivity(this);' to each activity you want to check with Apphance
    }

    boolean checkIfApphancePresent() {
        def startNewSession = { File it -> it.name.endsWith('.java') && it.text.contains('Apphance.startNewSession') }
        def apphanceLib = { File it -> it.name == 'apphance-library.jar' }

        allFiles(dir: variantDir, where: { startNewSession(it) || apphanceLib(it) }) || isApphanceActivityPresent(variantDir)
    }

    void addReportActivityToManifest() {

    }

    File getManifestFile() {
        new File(variantDir, ANDROID_MANIFEST)
    }
}
