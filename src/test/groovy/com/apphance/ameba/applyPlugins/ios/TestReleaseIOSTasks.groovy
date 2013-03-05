package com.apphance.ameba.applyPlugins.ios

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ios.plugins.buildplugin.IOSPlugin
import com.apphance.ameba.ios.plugins.release.IOSReleasePlugin
import com.apphance.ameba.plugins.release.ProjectReleasePlugin
import org.gradle.api.Project
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertTrue

class TestReleaseIOSTasks extends AbstractBaseIOSTaskTest {

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

    @Test
    public void testSetupTasksAvailable() {
        verifyTasksInGroup(getProject(), [
                'prepareSetup',
                'verifySetup',
                'showSetup',
                'showConventions',
        ], AmebaCommonBuildTaskGroups.AMEBA_SETUP)

        assertTrue(project.prepareSetup.prepareSetupOperations*.class.simpleName.containsAll(['PrepareIOSSetupOperation','PrepareReleaseSetupOperation']))
        assertTrue(project.verifySetup.verifySetupOperations*.class.simpleName.containsAll(['VerifyIOSSetupOperation','VerifyReleaseSetupOperation']))
        assertTrue(project.showSetup.showSetupOperations*.class.simpleName.containsAll(['ShowIOSSetupOperation','ShowReleaseSetupOperation']))
    }
}
