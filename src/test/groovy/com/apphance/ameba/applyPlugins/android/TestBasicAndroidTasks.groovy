package com.apphance.ameba.applyPlugins.android

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin
import org.gradle.api.Project
import org.junit.Test

class TestBasicAndroidTasks extends BaseAndroidTaskTest {

    @Override
    protected Project getProject() {
        Project project = super.getProject()
        project.project.plugins.apply(AndroidPlugin.class)
        return project
    }

    @Test
    public void testBuildTasksAvailable() {
        verifyTasksInGroup(getProject(), [
                'cleanAndroid',
                'cleanClasses',
                'compileAndroid',
                'buildAll',
                'buildAllDebug',
                'buildAllRelease',
                'buildDebug-test',
                'buildRelease-market',
                'installDebug-test',
                'installRelease-market',
                'replacePackage',
                'updateProject',
                'copySources'
        ], AmebaCommonBuildTaskGroups.AMEBA_BUILD)
    }

    @Test
    public void testConfigurationTasksAvailable() {
        verifyTasksInGroup(getProject(), [
                'cleanConfiguration',
                'readAndroidProjectConfiguration',
                'readAndroidVersionAndProjectName',
                'readProjectConfiguration',
        ], AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION)
    }

    @Test
    public void testReleaseTasksAvailable() {
        verifyTasksInGroup(getProject(), [], AmebaCommonBuildTaskGroups.AMEBA_RELEASE)
    }
}
