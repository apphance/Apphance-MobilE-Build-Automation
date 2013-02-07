package com.apphance.ameba.applyPlugins.android

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.android.plugins.apphance.AndroidApphancePlugin
import com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin
import org.gradle.api.Project
import org.junit.Test

class TestApphanceAndroidTasks extends BaseAndroidTaskTest {

    @Override
    protected Project getProject() {
        Project project = super.getProject(false)
        project.project.plugins.apply(AndroidPlugin.class)
        project.project.plugins.apply(AndroidApphancePlugin.class)
        return project
    }

    @Test
    public void testReportsTasksAvailable() {
        verifyTasksInGroup(getProject(), [
                'convertLogsToAndroid',
                'convertLogsToApphance',
                'removeApphanceFromManifest',
                'uploadDebug'
        ], AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE)
    }
}
