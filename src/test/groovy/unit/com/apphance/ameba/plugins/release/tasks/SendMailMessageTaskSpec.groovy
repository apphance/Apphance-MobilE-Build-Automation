package com.apphance.ameba.plugins.release.tasks

import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class SendMailMessageTaskSpec extends Specification {

    def p = ProjectBuilder.builder().build()
    def smt = p.task(SendMailMessageTask.NAME, type: SendMailMessageTask) as SendMailMessageTask

    def 'mail port is validated correctly when empty'() {
        when:
        smt.validateMailPort(mailPort)

        then:
        def e = thrown(GradleException)
        e.message =~ 'Property \'mail.port\' has invalid value!'

        where:
        mailPort << [null, '', '  \t', 'with letter', 'withletter', '123-123']
    }

    def 'mail port is validated correctly when set'() {
        when:
        smt.validateMailPort(mailPort)

        then:
        noExceptionThrown()

        where:
        mailPort << ['121', '1']
    }

    def 'mail server is validated correctly when empty'() {
        when:
        smt.validateMailServer(mailServer)

        then:
        def e = thrown(GradleException)
        e.message =~ 'Property \'mail.server\' has invalid value!'

        where:
        mailServer << [null, '  ', '  \t', 'with\tletter', 'with space']
    }

    def 'mail server is validated correctly when set'() {
        when:
        smt.validateMailServer(mailServer)

        then:
        noExceptionThrown()

        where:
        mailServer << ['releaseString', 'release_String', 'relase_String_123_4']
    }
}
