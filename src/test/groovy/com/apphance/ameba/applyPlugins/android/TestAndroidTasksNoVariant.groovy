package com.apphance.ameba.applyPlugins.android

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import org.junit.Test

class TestAndroidTasksNoVariant extends BaseAndroidTaskTest {

    @Test
    public void testBuildTasksAvailable() {
        verifyTasksInGroup(getProject(false), [
                'cleanAndroid',
                'cleanClasses',
                'compileAndroid',
                'buildAll',
                'buildAllDebug',
                'buildAllRelease',
                'buildDebug-Debug',
                'buildRelease-Release',
                'installDebug',
                'installRelease',
                'replacePackage',
                'updateProject',
                'copySources'
        ], AmebaCommonBuildTaskGroups.AMEBA_BUILD)
    }
}
