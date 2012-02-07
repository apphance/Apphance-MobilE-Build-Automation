package com.apphance.ameba.applyPlugins.android;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.android.plugins.AndroidPlugin
import com.apphance.ameba.android.plugins.AndroidTestPlugin

class TestTestAndroidTasks extends BaseAndroidTaskTest {
    protected Project getProject() {
        Project project = super.getProject(true)
        project.project.plugins.apply(AndroidPlugin.class)
        project.project.plugins.apply(AndroidTestPlugin.class)
        return project
    }

    @Test
    public void testBuildTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'testAndroid',
            'cleanAVD',
            'createAVD',
            'startEmulator',
            'stopAllEmulators'
        ],AmebaCommonBuildTaskGroups.AMEBA_TEST)
    }
}
