package com.apphance.flow.plugins.android.release.tasks

import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper
import com.apphance.flow.plugins.release.tasks.AbstractUpdateVersionTask

import javax.inject.Inject

class UpdateVersionTask extends AbstractUpdateVersionTask {

    String description = "Updates version stored in configuration file of the project - AndroidManifest.xml." +
            " Numeric version is set from 'version.code' system property (-D) or 'VERSION_CODE' environment variable " +
            "property. String version is set from 'version.string' system property (-D) or 'VERSION_CODE' " +
            "environment variable"

    @Inject AndroidManifestHelper manifestHelper
    @Inject AndroidVariantsConfiguration variantsConf

    @Override
    void updateDescriptor(String versionCode, String versionString) {
        variantsConf.variants.each { v ->
            manifestHelper.updateVersion(v.tmpDir, versionString, versionCode)
        }
    }
}
