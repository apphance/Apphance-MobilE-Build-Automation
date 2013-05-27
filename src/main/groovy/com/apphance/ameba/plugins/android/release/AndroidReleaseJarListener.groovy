package com.apphance.ameba.plugins.android.release

import com.apphance.ameba.plugins.android.builder.AndroidArtifactProvider
import com.apphance.ameba.plugins.android.builder.AndroidBuilderInfo

import javax.inject.Inject

/**
 * Listener that builds .jar file for library.
 *
 */
class AndroidReleaseJarListener implements AndroidBuildListener {

    @Inject AndroidArtifactProvider artifactProvider
    @Inject org.gradle.api.AntBuilder ant

    @Override
    void buildDone(AndroidBuilderInfo bi) {
        ant.copy(file: bi.originalFile, tofile: artifactProvider.jarArtifact(bi).location)
    }
}
