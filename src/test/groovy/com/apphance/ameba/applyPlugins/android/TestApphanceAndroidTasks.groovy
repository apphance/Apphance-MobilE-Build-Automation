package com.apphance.ameba.applyPlugins.android;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.android.plugins.apphance.AndroidApphancePlugin;
import com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin;

class TestApphanceAndroidTasks extends BaseAndroidTaskTest {
    protected Project getProject() {
        Project project = super.getProject(false)
        project.project.plugins.apply(AndroidPlugin.class)
        project.project.plugins.apply(AndroidApphancePlugin.class)
        return project
    }

    @Test
    public void testReportsTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'convertLogsToAndroid',
            'convertLogsToApphance',
            'removeApphanceFromManifest',
            'restoreManifestBeforeApphance'
        ],AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE)
    }
}
