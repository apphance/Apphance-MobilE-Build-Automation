package com.apphance.ameba.applyPlugins.android;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.android.plugins.build.AndroidPlugin;
import com.apphance.ameba.android.plugins.release.AndroidReleasePlugin
import com.apphance.ameba.plugins.release.ProjectReleasePlugin

class TestReleaseAndroidTasks extends BaseAndroidTaskTest {
    protected Project getProject() {
        Project project = super.getProject(false)
        project.project.plugins.apply(AndroidPlugin.class)
        project.project.plugins.apply(ProjectReleasePlugin.class)
        project.project.plugins.apply(AndroidReleasePlugin.class)
        return project
    }

    @Test
    public void testReportsTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'buildDocumentationZip',
            'buildSourcesZip',
            'prepareImageMontage',
            'prepareAvailableArtifactsInfo',
            'prepareMailMessage',
            'sendMailMessage',
            'cleanRelease',
            'updateVersion'
        ],AmebaCommonBuildTaskGroups.AMEBA_RELEASE)
    }
}
