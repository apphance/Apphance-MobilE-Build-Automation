package com.apphance.ameba.applyPlugins.ios;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ios.plugins.build.IOSPlugin;
import com.apphance.ameba.ios.plugins.framework.IOSFrameworkPlugin

class TestFrameworkIOSTasks extends BaseIOSTaskTest {

    protected Project getProject() {
        Project project = super.getProject()
        project.project.plugins.apply(IOSPlugin.class)
        project.project.plugins.apply(IOSFrameworkPlugin.class)
        return project
    }

    @Test
    public void testBuildTasksAvailable() {
        verifyTasksInGroup(getProject(),[
            'clean',
            'buildAll',
            'buildAllSimulators',
            'build-GradleXCode-BasicConfiguration',
            'buildSingleRelease',
            'buildFramework',
            'checkTests',
            'copyMobileProvision',
            'replaceBundleIdPrefix',
            'unlockKeyChain'
        ],AmebaCommonBuildTaskGroups.AMEBA_BUILD)
    }
}
