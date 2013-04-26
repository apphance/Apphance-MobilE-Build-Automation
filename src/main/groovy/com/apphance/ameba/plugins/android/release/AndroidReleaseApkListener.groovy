package com.apphance.ameba.plugins.android.release

import com.apphance.ameba.plugins.android.AndroidArtifactBuilder
import com.apphance.ameba.plugins.android.AndroidBuilderInfo
import org.gradle.api.Project

/**
 * Build listener that provides .apk file in ota dir.
 *
 */
class AndroidReleaseApkListener implements AndroidBuildListener {

    private AndroidArtifactBuilder artifactBuilder

    AndroidReleaseApkListener(AndroidArtifactBuilder artifactBuilder) {
        this.artifactBuilder = artifactBuilder
    }

    @Override
    void buildDone(Project project, AndroidBuilderInfo bi) {
        project.ant {
            copy(file: bi.originalFile, tofile: artifactBuilder.apkArtifact(bi).location)
        }
    }
}
