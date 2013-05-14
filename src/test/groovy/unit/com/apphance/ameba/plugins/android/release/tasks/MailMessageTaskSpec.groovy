package com.apphance.ameba.plugins.android.release.tasks

import org.gradle.api.GradleException
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder

class MailMessageTaskSpec extends Specification {

    def p = builder().build()
    def mmt = p.task(MailMessageTask.NAME, type: MailMessageTask) as MailMessageTask

    def 'release notes are validated correctly when empty'() {
        when:
        mmt.validateReleaseNotes(releaseNotes)

        then:
        def e = thrown(GradleException)
        e.message =~ 'Release notes are empty'

        where:
        releaseNotes << [[], null]
    }

    def 'release notes are validated correctly when set'() {
        when:
        mmt.validateReleaseNotes(releaseNotes)

        then:
        noExceptionThrown()

        where:
        releaseNotes << [['1', '2', '3'], ['', '2']]
    }
}
