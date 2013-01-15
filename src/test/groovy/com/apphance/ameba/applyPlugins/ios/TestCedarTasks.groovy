package com.apphance.ameba.applyPlugins.ios

import com.apphance.ameba.ios.plugins.buildplugin.IOSPlugin
import com.apphance.ameba.ios.plugins.cedar.CedarPlugin
import org.gradle.api.Project
import org.junit.Test

class TestCedarTasks extends AbstractBaseIOSTaskTest {

    protected Project getProject() {
        Project project = super.getProject()
        project.project.plugins.apply(IOSPlugin.class)
        project.project.plugins.apply(CedarPlugin.class)
        return project
    }

    @Test
    public void testCedarTasksAvailable() {
        verifyTasksInGroup(getProject(), [
                'buildCedarReleases',
                'prepareCedarTemplates',
                'runCedarTests',
        ], CedarPlugin.AMEBA_IOS_CEDAR)
    }
}
