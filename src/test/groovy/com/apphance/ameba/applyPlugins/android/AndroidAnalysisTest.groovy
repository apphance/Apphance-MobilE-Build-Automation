package com.apphance.ameba.applyPlugins.android;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.android.plugins.AndroidAnalysisPlugin
import com.apphance.ameba.android.plugins.AndroidPlugin

class AndroidAnalysisTest extends BaseAndroidTaskTest {

    protected Project getProject() {
        Project project = super.getProject()
        project.project.plugins.apply(AndroidPlugin.class)
        project.project.plugins.apply(AndroidAnalysisPlugin.class)
        return project
    }

    @Test
    public void testTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'analysis',
            'checkstyle',
            'cpd',
            'findbugs',
            'pmd',
        ],AmebaCommonBuildTaskGroups.AMEBA_ANALYSIS)
    }
}
