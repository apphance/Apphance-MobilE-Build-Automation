package com.apphance.ameba.plugins.android.release.tasks

import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class UpdateVersionTaskSpec extends Specification {

    def p = ProjectBuilder.builder().build()
    def uvt = p.task(UpdateVersionTask.NAME, type: UpdateVersionTask) as UpdateVersionTask

    def 'release code is validated correctly when empty'() {
        when:
        uvt.validateReleaseCode(code)

        then:
        def e = thrown(GradleException)
        e.message =~ 'Property \'release.code\' has invalid value!'

        where:
        code << [null, '', '  \t', 'with letter', 'withletter', '123-123']
    }

    def 'release code is validated correctly when set'() {
        when:
        uvt.validateReleaseCode(code)

        then:
        noExceptionThrown()

        where:
        code << ['121', '1']
    }

    def 'release string is validated correctly when empty'() {
        when:
        uvt.validateReleaseString(code)

        then:
        def e = thrown(GradleException)
        e.message =~ 'Property \'release.string\' has invalid value!'

        where:
        code << [null, '  ', '  \t', 'with\tletter', 'with space']
    }

    def 'release string is validated correctly when set'() {
        when:
        uvt.validateReleaseString(code)

        then:
        noExceptionThrown()

        where:
        code << ['releaseString', 'release_String', 'relase_String_123_4']
    }
}
