package com.apphance.ameba.env

import spock.lang.Specification

import static com.apphance.ameba.env.Environment.JENKINS
import static com.apphance.ameba.env.Environment.LOCAL
import static com.apphance.ameba.env.JenkinsEnvVariables.*

class EnvironmentSpec extends Specification {

    def 'env is detected according to variables map'() {
        given:
        System.metaClass.static.getenv = {
            variables
        }

        expect:
        Environment.env() == env

        where:
        env     | variables
        LOCAL   | [(WORKSPACE.name()): 'workspace', (JENKINS_URL.name()): 'jenkins_url']
        LOCAL   | [:]
        JENKINS | [(JOB_URL.name()): 'job_url', (WORKSPACE.name()): 'workspace', (JENKINS_URL.name()): 'jenkins_url']
        LOCAL   | [(JOB_URL.name()): '', (WORKSPACE.name()): 'workspace', (JENKINS_URL.name()): 'jenkins_url']
    }
}
