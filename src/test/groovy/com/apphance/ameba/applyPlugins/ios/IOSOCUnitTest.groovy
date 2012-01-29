package com.apphance.ameba.applyPlugins.ios;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test

import com.apphance.ameba.ios.plugins.IOSPlugin
import com.apphance.ameba.ios.plugins.IOSUnitTestPlugin

class IOSOCUnitTest extends BaseIOSTaskTest {

    protected Project getProject() {
        Project project = super.getProject()
        project.project.plugins.apply(IOSPlugin.class)
        project.project.plugins.apply(IOSUnitTestPlugin.class)
        return project
    }

    @Test
    public void testCOCUnitTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'runUnitTests',
        ],IOSUnitTestPlugin.AMEBA_IOS_UNIT)
    }
}
