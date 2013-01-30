package com.apphance.ameba.applyPlugins.android

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin
import com.apphance.ameba.android.plugins.release.AndroidReleasePlugin
import com.apphance.ameba.plugins.release.ProjectReleasePlugin
import org.gradle.api.Project
import org.junit.Test

class TestReleaseAndroidTasks extends BaseAndroidTaskTest {

    @Override
    protected Project getProject() {
        Project project = super.getProject(false)
        project.project.plugins.apply(AndroidPlugin.class)
        project.project.plugins.apply(ProjectReleasePlugin.class)
        project.project.plugins.apply(AndroidReleasePlugin.class)
        return project
    }

    @Test
    public void testReleaseTasksAvailable() {
        verifyTasksInGroup(getProject(), [
                'cleanRelease',
                'updateVersion',
                'buildDocumentationZip',
                'buildSourcesZip',
                'prepareAvailableArtifactsInfo',
                'prepareForRelease',
                'prepareImageMontage',
                'prepareMailMessage',
                'sendMailMessage',
                'verifyReleaseNotes'
        ], AmebaCommonBuildTaskGroups.AMEBA_RELEASE)
    }
}
