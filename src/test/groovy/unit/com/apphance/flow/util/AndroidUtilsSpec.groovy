package com.apphance.flow.util

import spock.lang.Specification

@Mixin(AndroidUtils)
class AndroidUtilsSpec extends Specification {

    def 'test find libraries'() {
        expect:
        allLibraries(new File('projects/test/android/android-basic'))*.path ==
                ['projects/test/android/android-basic/subproject', 'projects/test/android/android-basic/subproject/subsubproject']
    }
}
