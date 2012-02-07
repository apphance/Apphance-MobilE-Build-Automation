package com.apphance.ameba.applyPlugins.ios;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test

import com.apphance.ameba.ios.plugins.FoneMonkeyPlugin
import com.apphance.ameba.ios.plugins.IOSPlugin

class IOSFoneMonkeyTest extends BaseIOSTaskTest {

    protected Project getProject() {
        Project project = super.getProject()
        project.project.plugins.apply(IOSPlugin.class)
        project.project.plugins.apply(FoneMonkeyPlugin.class)
        return project
    }

    @Test
    public void testMonkeyTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'buildFoneMonkeyRelease',
            'prepareFoneMonkeyTemplates',
            'runMonkeyTests',
            'runSingleFoneMonkeyTest',
        ],FoneMonkeyPlugin.AMEBA_IOS_FONEMONKEY)
    }
}
