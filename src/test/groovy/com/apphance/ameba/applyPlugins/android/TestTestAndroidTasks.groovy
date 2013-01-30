package com.apphance.ameba.applyPlugins.android

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin
import com.apphance.ameba.android.plugins.test.AndroidTestPlugin
import org.gradle.api.Project
import org.junit.Test

class TestTestAndroidTasks extends BaseAndroidTaskTest {

    @Override
    protected Project getProject() {
        Project project = super.getProject(true)
        project.project.plugins.apply(AndroidPlugin.class)
        project.project.plugins.apply(AndroidTestPlugin.class)
        return project
    }

    @Test
    public void testBuildTasksAvailable() {
        verifyTasksInGroup(getProject(), [
                'checkTests',
                'testAndroid',
                'cleanAVD',
                'createAVD',
                'startEmulator',
                'stopAllEmulators',
                'prepareRobotium',
                'prepareRobolectric',
                'testRobolectric'
        ], AmebaCommonBuildTaskGroups.AMEBA_TEST)
    }
}
