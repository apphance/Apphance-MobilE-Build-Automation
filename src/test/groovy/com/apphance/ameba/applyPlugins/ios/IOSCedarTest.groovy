package com.apphance.ameba.applyPlugins.ios;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test

import com.apphance.ameba.ios.plugins.build.IOSPlugin;
import com.apphance.ameba.ios.plugins.cedar.CedarPlugin;

class IOSCedarTest extends BaseIOSTaskTest {

    protected Project getProject() {
        Project project = super.getProject()
        project.project.plugins.apply(IOSPlugin.class)
        project.project.plugins.apply(CedarPlugin.class)
        return project
    }

    @Test
    public void testCedarTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'buildCedarReleases',
            'prepareCedarTemplates',
            'runCedarTests',
        ],CedarPlugin.AMEBA_IOS_CEDAR)
    }
}
