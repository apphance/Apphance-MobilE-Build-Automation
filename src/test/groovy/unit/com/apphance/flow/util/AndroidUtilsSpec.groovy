package com.apphance.flow.util

import spock.lang.Specification

@Mixin(AndroidUtils)
class AndroidUtilsSpec extends Specification {

    def 'test find libraries'() {
        expect:
        allLibraries(new File('testProjects/android/android-basic'))*.path ==
                ['testProjects/android/android-basic/subproject', 'testProjects/android/android-basic/subproject/subsubproject']
    }
}
