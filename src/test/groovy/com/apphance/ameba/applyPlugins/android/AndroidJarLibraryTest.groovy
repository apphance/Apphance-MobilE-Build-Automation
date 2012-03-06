package com.apphance.ameba.applyPlugins.android;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin;
import com.apphance.ameba.android.plugins.jarlibrary.AndroidJarLibraryPlugin;

class AndroidJarLibraryTest extends BaseAndroidTaskTest {

    protected Project getProject() {
        Project project = super.getProject()
        project.project.plugins.apply(AndroidPlugin.class)
        project.project.plugins.apply(AndroidJarLibraryPlugin.class)
        return project
    }

    @Test
    public void testTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'buildAll',
            'buildAllDebug',
            'buildDebug-test',
            'buildAllRelease',
            'buildRelease-market',
            'cleanAndroid',
            'cleanClasses',
            'compileAndroid',
            'installDebug-test',
            'installRelease-market',
            'jarLibrary',
            'replacePackage',
            'updateProject',
			'copySources'
        ],AmebaCommonBuildTaskGroups.AMEBA_BUILD)
    }
}
