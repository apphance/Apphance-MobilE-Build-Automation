package com.apphance.ameba.plugins.android.release.tasks

import com.apphance.ameba.plugins.android.parsers.AndroidManifestHelper
import com.apphance.ameba.plugins.release.tasks.AbstractUpdateVersionTask

import javax.inject.Inject

class UpdateVersionTask extends AbstractUpdateVersionTask {

    @Inject AndroidManifestHelper manifestHelper

    @Override
    void updateDescriptor(String versionCode, String versionString) {
        manifestHelper.updateVersion(conf.rootDir, versionString, versionCode)
    }
}
