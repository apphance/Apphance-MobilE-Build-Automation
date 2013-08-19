package com.apphance.flow.plugins.release.tasks

import com.apphance.flow.TestUtils
import com.apphance.flow.configuration.properties.ListStringProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.configuration.release.ReleaseConfiguration
import com.apphance.flow.plugins.release.FlowArtifact
import spock.lang.Specification

@Mixin(TestUtils)
class SendMailMessageTaskSpec extends Specification {

    def task = create(SendMailMessageTask) as SendMailMessageTask

    def releaseConf = GroovySpy(ReleaseConfiguration) {
        getMailServer() >> 'mail.server'
        getMailPort() >> '3145'
        getReleaseMailFrom() >> new StringProperty(value: 'release@mail.from')
        getReleaseMailTo() >> new StringProperty(value: 'release@mail.to')
        getReleaseMailSubject() >> 'Release mail'
        getMailMessageFile() >> GroovyMock(FlowArtifact) {
            getLocation() >> GroovyMock(File) {
                getText() >> 'msg'
            }
        }
        getReleaseMailFlags() >> new ListStringProperty(value: [])
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
        noExceptionThrown()

        and:
        System.properties['mail.smtp.host'] == 'mail.server'
        System.properties['mail.smtp.port'] == '3145'

        and:
        1 * ant.mail([
                'mailhost': 'mail.server',
                'mailport': '3145',
                'subject': 'Release mail',
                'charset': 'UTF-8',
                'tolist': 'release@mail.to',
                'from': 'release@mail.from',
                'message': 'msg',
                'messageMimeType': 'text/html',
                'files': ''
        ])
    }
}
