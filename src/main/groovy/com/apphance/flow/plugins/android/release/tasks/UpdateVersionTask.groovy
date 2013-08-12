package com.apphance.flow.plugins.android.release.tasks

import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper
import com.apphance.flow.plugins.release.tasks.AbstractUpdateVersionTask

import javax.inject.Inject

class UpdateVersionTask extends AbstractUpdateVersionTask {

    @Inject AndroidManifestHelper manifestHelper
    @Inject AndroidVariantsConfiguration variantsConf

    @Override
    void updateDescriptor(String versionCode, String versionString) {
        variantsConf.variants.each { AndroidVariantConfiguration variantConf ->
            logger.lifecycle "Updating version string: $versionString, version code: $versionCode in $variantConf.tmpDir.absolutePath"
            manifestHelper.updateVersion(variantConf.tmpDir, versionString, versionCode)
        }
    }
}
