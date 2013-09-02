package com.apphance.flow.util

import spock.lang.Specification

@Mixin(FlowUtils)
class FlowUtilsSpec extends Specification {

    private File rootSearchDir = new File('src/test/resources/com/apphance/flow/util')

    def 'test allFiles for non existing directory'() {
        expect:
        allFiles(dir: new File('/some-nonexisting-directory')) == []
    }

    def 'search without filter'() {
        when:
        def files = allFiles dir: rootSearchDir

        then:
        files.size() == 4
    }

    def 'test search with filter'() {
        when:
        def files = allFiles dir: rootSearchDir, where: {it.name ==~ /.*.dat/}

        then:
        files.sort()*.name == ['testFile1.dat', 'testFile3.dat']
    }

    def 'test getPackage'() {
        expect:
        getPackage(file) == expectedPackage

        where:
        file                                                   | expectedPackage
        tempFile << 'package com.polidea;'                     | 'com.polidea'
        tempFile << 'com.polidea;'                             | ''
        tempFile << '\n\npackage com.polidea;\n'               | 'com.polidea'
        tempFile << '\n\npackage \tcom.polidea  ;  \n'         | 'com.polidea'
        tempFile << '\n\n   \tpackage \t com.polidea \t ;  \n' | 'com.polidea'
    }
}
