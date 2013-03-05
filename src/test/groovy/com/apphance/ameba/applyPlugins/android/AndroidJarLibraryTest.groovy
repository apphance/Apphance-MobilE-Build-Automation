package com.apphance.ameba.applyPlugins.android

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import org.junit.Test

class AndroidJarLibraryTest extends BaseAndroidTaskTest {

    @Test
    public void testTasksAvailable() {
        verifyTasksInGroup(getProject(), [
                'buildAll',
                'buildAllDebug',
                'buildDebug-test',
                'buildAllRelease',
                'buildRelease-market',
                'cleanAndroid',
                'cleanClasses',
                'compileAndroid',
                'installDebug-test',
                'installRelease-market',
                'jarLibrary',
                'replacePackage',
                'updateProject',
                'copySources'
        ], AmebaCommonBuildTaskGroups.AMEBA_BUILD)
    }
}
