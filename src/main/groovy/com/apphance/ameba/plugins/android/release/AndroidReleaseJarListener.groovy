package com.apphance.ameba.plugins.android.release

import com.apphance.ameba.plugins.android.AndroidArtifactBuilder
import com.apphance.ameba.plugins.android.AndroidBuilderInfo
import org.gradle.api.Project

/**
 * Listener that builds .jar file for library.
 *
 */
class AndroidReleaseJarListener implements AndroidBuildListener {

    private AndroidArtifactBuilder artifactBuilder

    AndroidReleaseJarListener(AndroidArtifactBuilder artifactBuilder) {
        this.artifactBuilder = artifactBuilder
    }

    @Override
    void buildDone(Project project, AndroidBuilderInfo bi) {
        project.ant {
            copy(file: bi.originalFile, tofile: artifactBuilder.jarArtifact(bi).location)
        }
    }
}
