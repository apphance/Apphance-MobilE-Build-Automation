package com.apphance.ameba.applyPlugins.ios;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test

import com.apphance.ameba.ios.plugins.KIFPlugin
import com.apphance.ameba.ios.plugins.IOSPlugin

class IOSKIFTest extends BaseIOSTaskTest {

    protected Project getProject() {
        Project project = super.getProject()
        project.project.plugins.apply(IOSPlugin.class)
        project.project.plugins.apply(KIFPlugin.class)
        return project
    }

    @Test
    public void testKIFTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'buildKIFRelease',
            'prepareKIFTemplates',
            'runKIFTests',
            'runSingleKIFTest',
        ],KIFPlugin.AMEBA_IOS_KIF)
    }
}
