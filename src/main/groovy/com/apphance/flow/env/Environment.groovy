package com.apphance.flow.env

import static com.apphance.flow.env.JenkinsEnvVariables.*
import static org.apache.commons.lang.StringUtils.isNotBlank

enum Environment {
    JENKINS, LOCAL

    static Environment env() {
        jenkinsVariablesDeclared() ? JENKINS : LOCAL
    }

    private static boolean jenkinsVariablesDeclared() {
        def env = System.getenv()
        [env[JENKINS_URL.name()], env[JOB_URL.name()], env[WORKSPACE.name()]].every { isNotBlank(it) }
    }
}