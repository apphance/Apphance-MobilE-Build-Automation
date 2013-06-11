package com.apphance.flow.plugins.android.release.tasks

import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper
import com.apphance.flow.plugins.release.tasks.AbstractUpdateVersionTask

import javax.inject.Inject

class UpdateVersionTask extends AbstractUpdateVersionTask {

    @Inject AndroidManifestHelper manifestHelper

    @Override
    void updateDescriptor(String versionCode, String versionString) {
        manifestHelper.updateVersion(conf.rootDir, versionString, versionCode)
    }
}
