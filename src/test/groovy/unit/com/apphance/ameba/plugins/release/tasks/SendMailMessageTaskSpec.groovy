package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.TestUtils
import org.gradle.api.GradleException
import spock.lang.Specification

@Mixin(TestUtils)
class SendMailMessageTaskSpec extends Specification {

    def task = create(SendMailMessageTask)

    def 'mail port is validated correctly when empty'() {
        when:
        task.validateMailPort(mailPort)

        then:
        def e = thrown(GradleException)
        e.message =~ 'Property \'mail.port\' has invalid value!'

        where:
        mailPort << [null, '', '  \t', 'with letter', 'withletter', '123-123']
    }

    def 'mail port is validated correctly when set'() {
        when:
        task.validateMailPort(mailPort)

        then:
        noExceptionThrown()

        where:
        mailPort << ['121', '1']
    }

    def 'mail server is validated correctly when empty'() {
        when:
        task.validateMailServer(mailServer)

        then:
        def e = thrown(GradleException)
        e.message =~ 'Property \'mail.server\' has invalid value!'

        where:
        mailServer << [null, '  ', '  \t', 'with\tletter', 'with space']
    }

    def 'mail server is validated correctly when set'() {
        when:
        task.validateMailServer(mailServer)

        then:
        noExceptionThrown()

        where:
        mailServer << ['releaseString', 'release_String', 'relase_String_123_4']
    }
}
