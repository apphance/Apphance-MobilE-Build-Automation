package com.apphance.ameba.applyPlugins.ios;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ios.plugins.build.IOSPlugin;
import com.apphance.ameba.ios.plugins.release.IOSReleasePlugin;

class IOSReportsTest extends BaseIOSTaskTest {

    protected Project getProject() {
        Project project = super.getProject()
        project.project.plugins.apply(IOSPlugin.class)
        project.project.plugins.apply(IOSReleasePlugin.class)
        return project
    }

    @Test
    public void testReportsTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'buildDocumentationZip',
            'buildSourcesZip',
            'prepareImageMontage',
        ],AmebaCommonBuildTaskGroups.AMEBA_RELEASE)
    }

    @Test
    public void testMessagingTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'prepareAvailableArtifactsInfo',
            'prepareMailMessage',
            'sendMailMessage',
        ],AmebaCommonBuildTaskGroups.AMEBA_RELEASE)
    }

    @Test
    public void testRelaseTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'cleanRelease',
            'updateVersion',
        ],AmebaCommonBuildTaskGroups.AMEBA_RELEASE)
    }
}
