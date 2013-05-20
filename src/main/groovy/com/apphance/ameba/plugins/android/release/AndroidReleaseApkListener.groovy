package com.apphance.ameba.plugins.android.release

import com.apphance.ameba.plugins.android.builder.AndroidArtifactProvider
import com.apphance.ameba.plugins.android.builder.AndroidBuilderInfo

import javax.inject.Inject

/**
 * Build listener that provides .apk file in ota dir.
 *
 */
class AndroidReleaseApkListener implements AndroidBuildListener {

    @Inject
    AndroidArtifactProvider artifactProvider
    @Inject
    AntBuilder ant

    @Override
    void buildDone(AndroidBuilderInfo bi) {
        ant.copy(file: bi.originalFile, tofile: artifactProvider.apkArtifact(bi).location)
    }
}
