package com.apphance.flow.util

import spock.lang.Specification

@Mixin(AndroidUtils)
class AndroidUtilsSpec extends Specification {

    def 'test find libraries'() {
        expect:
        allLibraries(new File('demo/android/android-basic'))*.path ==
                ['demo/android/android-basic/subproject', 'demo/android/android-basic/subproject/subsubproject']
    }
}
