package com.apphance.ameba.env

import spock.lang.Specification

import static com.apphance.ameba.env.Environment.JENKINS
import static com.apphance.ameba.env.Environment.LOCAL
import static com.apphance.ameba.env.JenkinsEnvVariables.*

class EnvironmentSpec extends Specification {

    def 'local env is detected'() {
        expect:
        Environment.env() == LOCAL
    }

    def 'jenkins env is detected'() {
        given:
        System.metaClass.static.getenv = {
            [
                    (JOB_URL.name()): 'job_url',
                    (WORKSPACE.name()): 'workspace',
                    (JENKINS_URL.name()): 'jenkins_url',
            ]
        }

        expect:
        Environment.env() == JENKINS
    }
}
