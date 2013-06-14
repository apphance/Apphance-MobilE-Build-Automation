package com.apphance.flow.plugins.android.apphance.tasks

import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.apphance.ApphanceMode
import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper
import com.apphance.flow.util.FlowUtils
import groovy.util.slurpersupport.GPathResult
import org.gradle.api.GradleException

import static android.Manifest.permission.*
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

        addReportActivityToManifest()
        addPermisions()
        // Add 'Apphance.startNewSession(...)' call to the main activity
        // Add 'Apphance.setCurrentActitivity(this);' to each activity you want to check with Apphance
    }

    boolean checkIfApphancePresent() {
        def startNewSession = { File it -> it.name.endsWith('.java') && it.text.contains('Apphance.startNewSession') }
        def apphanceLib = { File it -> it.name == 'apphance-library.jar' }

        allFiles(dir: variantDir, where: { startNewSession(it) || apphanceLib(it) }) || isApphanceActivityPresent(variantDir)
    }

    void addReportActivityToManifest() {
        withManifest(variantDir) { GPathResult manifest ->
            manifest.application.appendNode {
                activity('android:name': 'com.apphance.android.ui.ProblemActivity',
                        'android:configChanges': 'orientation',
                        'android:launchMode': 'singleInstance',
                        'android:process': 'com.utest.apphance.reporteditor')
            }
        }
    }

    void addPermisions() {
        withManifest(variantDir) { GPathResult manifest ->
            addPermissionsToManifest(manifest, INTERNET, READ_PHONE_STATE, GET_TASKS, ACCESS_WIFI_STATE, ACCESS_NETWORK_STATE,
                    ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, BLUETOOTH)
        }
    }
}
