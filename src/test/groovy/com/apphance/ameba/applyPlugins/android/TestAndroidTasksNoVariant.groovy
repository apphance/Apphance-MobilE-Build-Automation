package com.apphance.ameba.applyPlugins.android;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin;

class TestAndroidTasksNoVariant extends BaseAndroidTaskTest {
    protected Project getProject() {
        Project project = super.getProject(false)
        project.project.plugins.apply(AndroidPlugin.class)
        return project
    }

    @Test
    public void testBuildTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'cleanAndroid',
            'cleanClasses',
            'compileAndroid',
            'buildAll',
            'buildAllDebug',
            'buildAllRelease',
			'buildDebug',
			'buildRelease',
            'checkTests',
            'installDebug',
            'installRelease',
            'replacePackage',
            'updateProject',
			'copySources'
        ],AmebaCommonBuildTaskGroups.AMEBA_BUILD)
    }
}
