package com.apphance.flow.util

import spock.lang.Specification

@Mixin(FlowUtils)
class FlowUtilsSpec extends Specification {

    private File rootSearchDir = new File('src/test/resources/com/apphance/flow/util')

    def 'check argument'() {
        when:
        allFiles dir: new File('/some-nonexisting-directory')

        then:
        thrown(IllegalArgumentException)
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

    def 'test equals ignore whitespace'() {
        expect:
        equalsIgnoreWhitespace('abc', 'abc')
        equalsIgnoreWhitespace('', '')
        equalsIgnoreWhitespace('', '   ')
        equalsIgnoreWhitespace('a b c', 'abc')
        equalsIgnoreWhitespace("\n\na b c\n", 'abc    ')
    }
}
