package com.apphance.ameba.applyPlugins.ios;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ios.plugins.IOSPlugin
import com.apphance.ameba.ios.plugins.IOSReportsPlugin

class IOSReportsTest extends BaseIOSTaskTest {

    protected Project getProject() {
        Project project = super.getProject()
        project.project.plugins.apply(IOSPlugin.class)
        project.project.plugins.apply(IOSReportsPlugin.class)
        return project
    }

    @Test
    public void testReportsTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'buildDocumentationZip',
            'buildSourcesZip',
            'prepareImageMontage',
        ],AmebaCommonBuildTaskGroups.AMEBA_REPORTS)
    }

    @Test
    public void testMessagingTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'prepareAvailableArtifactsInfo',
            'prepareMailMessage',
            'sendMailMessage',
        ],AmebaCommonBuildTaskGroups.AMEBA_MESSAGING)
    }

    @Test
    public void testRelaseTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'cleanRelease',
            'postRelease',
            'preRelease',
            'updateVersion',
        ],AmebaCommonBuildTaskGroups.AMEBA_RELEASE)
    }
}
