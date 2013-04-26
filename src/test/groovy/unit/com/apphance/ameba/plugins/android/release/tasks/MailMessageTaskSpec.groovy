package com.apphance.ameba.plugins.android.release.tasks

import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class MailMessageTaskSpec extends Specification {

    def p = ProjectBuilder.builder().build()
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
