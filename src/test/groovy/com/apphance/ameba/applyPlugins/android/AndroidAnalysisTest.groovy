package com.apphance.ameba.applyPlugins.android

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.android.plugins.analysis.AndroidAnalysisPlugin
import com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin
import org.gradle.api.Project
import org.junit.Test

class AndroidAnalysisTest extends BaseAndroidTaskTest {

    protected Project getProject() {
        Project project = super.getProject()
        project.project.plugins.apply(AndroidPlugin.class)
        project.project.plugins.apply(AndroidAnalysisPlugin.class)
        return project
    }

    @Test
    public void testTasksAvailable() {
        verifyTasksInGroup(getProject(), [
                'analysis',
                'checkstyle',
                'cpd',
                'findbugs',
                'pmd',
        ], AmebaCommonBuildTaskGroups.AMEBA_ANALYSIS)
    }
}
