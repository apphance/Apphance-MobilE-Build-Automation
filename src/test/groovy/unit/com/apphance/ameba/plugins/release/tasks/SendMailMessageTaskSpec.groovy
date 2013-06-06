package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.TestUtils
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.release.ReleaseConfiguration
import spock.lang.Specification

@Mixin(TestUtils)
class SendMailMessageTaskSpec extends Specification {

    def task = create(SendMailMessageTask) as SendMailMessageTask

    def releaseConf = GroovySpy(ReleaseConfiguration) {
        getMailServer() >> 'mail.server'
        getMailPort() >> '3145'
        getReleaseMailFrom() >> new StringProperty(value: 'relase@mail.from')
        getReleaseMailTo() >> new StringProperty(value: 'relase@mail.to')
    }

    def ant = GroovyMock(org.gradle.api.AntBuilder)

    def setup() {
        task.releaseConf = releaseConf
        task.ant = ant
    }

    def 'ant mailer is called'() {
        given:
        task.project.configurations.create('mail')

        when:
        task.sendMailMessage()

        then:
        1 * ant.mail(_, _)
    }
}
