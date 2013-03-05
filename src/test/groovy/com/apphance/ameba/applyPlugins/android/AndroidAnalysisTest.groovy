package com.apphance.ameba.applyPlugins.android

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import org.junit.Test

class AndroidAnalysisTest extends BaseAndroidTaskTest {

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
