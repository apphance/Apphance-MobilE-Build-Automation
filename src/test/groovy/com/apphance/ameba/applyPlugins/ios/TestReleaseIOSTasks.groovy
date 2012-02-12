package com.apphance.ameba.applyPlugins.ios;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ios.plugins.build.IOSPlugin;
import com.apphance.ameba.ios.plugins.release.IOSReleasePlugin
import com.apphance.ameba.plugins.release.ProjectReleasePlugin;

class TestReleaseIOSTasks extends BaseIOSTaskTest {

    protected Project getProject() {
        Project project = super.getProject()
        project.project.plugins.apply(IOSPlugin.class)
        project.project.plugins.apply(ProjectReleasePlugin.class)
        project.project.plugins.apply(IOSReleasePlugin.class)
        return project
    }

    @Test
    public void testReleaseTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'cleanRelease',
            'updateVersion',
            'buildDocumentationZip',
            'buildSourcesZip',
            'copyGalleryFiles',
            'prepareAvailableArtifactsInfo',
            'prepareForRelease',
            'prepareImageMontage',
            'prepareMailMessage',
            'sendMailMessage',
            'verifyReleaseNotes'
        ],AmebaCommonBuildTaskGroups.AMEBA_RELEASE)
    }

    @Test
    public void testSetupTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'prepareBaseSetup',
            'prepareSetup',
            'prepareIOSSetup',
            'prepareReleaseSetup',
            'verifyBaseSetup',
            'verifySetup',
            'verifyIOSSetup',
            'verifyReleaseSetup',
            'showBaseSetup',
            'showSetup',
            'showIOSSetup',
            'showReleaseSetup',
        ],AmebaCommonBuildTaskGroups.AMEBA_SETUP)
    }
}
