package com.apphance.flow.plugins.ios.apphance.tasks

import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.configuration.reader.PropertyReader
import org.gradle.api.GradleException
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder

class IOSApphanceUploadTaskTestSpec extends Specification {

    def project = builder().build()
    def task = project.task('UploadTask', type: IOSApphanceUploadTask) as IOSApphanceUploadTask

    def 'exception is thrown when user, pass or key empty'() {
        given:
        task.variant = GroovyMock(AbstractIOSVariant) {
            getApphanceAppKey() >> new StringProperty(value: null)
            getName() >> 'Variant1'
        }
        task.apphanceConf = GroovyMock(ApphanceConfiguration) {
            getUser() >> new StringProperty(value: user)
            getPass() >> new StringProperty(value: pass)
        }
        task.reader = GroovyMock(PropertyReader) {
            systemProperty('apphance.user') >> user
            systemProperty('apphance.pass') >> pass
            envVariable('APPHANCE_USER') >> user
            envVariable('APPHANCE_PASS') >> pass
        }

        when:
        task.upload()

        then:
        def e = thrown(GradleException)
        e.message == message

        where:
        user   | pass   | message
        null   | null   | "Impossible to find user name for apphance.com! Define it in flow.properties configuration file or 'apphance.user' system property or 'APPHANCE_USER' environment variable!"
        'user' | null   | "Impossible to find password for apphance.com! Define it in flow.properties configuration file or 'apphance.pass' system property or 'APPHANCE_PASS' environment variable!"
        'user' | 'pass' | 'Impossible to find apphance key for variant: Variant1. Define it in appropriate section of flow.properties file!'
    }
}
