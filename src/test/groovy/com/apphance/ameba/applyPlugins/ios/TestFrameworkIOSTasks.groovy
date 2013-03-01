package com.apphance.ameba.applyPlugins.ios

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ios.plugins.buildplugin.IOSPlugin
import com.apphance.ameba.ios.plugins.framework.IOSFrameworkPlugin
import com.apphance.ameba.plugins.AmebaPlugin
import org.gradle.api.Project
import org.junit.Test

class TestFrameworkIOSTasks extends AbstractBaseIOSTaskTest {

    @Test
    public void testBuildTasksAvailable() {
        verifyTasksInGroup(getProject(), [
                'clean',
                'buildAll',
                'buildAllSimulators',
                'build-GradleXCode-BasicConfiguration',
                'buildSingleVariant',
                'buildFramework',
                'copyMobileProvision',
                'unlockKeyChain',
                'copySources',
                'copyDebugSources',
        ], AmebaCommonBuildTaskGroups.AMEBA_BUILD)
    }
}
