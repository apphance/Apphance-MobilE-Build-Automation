package com.apphance.ameba.plugins.projectconfiguration.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_SETUP

/**
 * Verifies all properties.
 *
 */
class VerifySetupTask extends DefaultTask {

    static String NAME = 'verifySetup'
    String description = 'Verifies if the project can be build properly'
    String group = AMEBA_SETUP

    @TaskAction
    void verifySetup() {
        List errors = []
        project.confsToVerify.each {
            it.verify()
            errors += it.errors
        }

        if (errors) {
            errors.each { println "ERROR: $it" }
            throw new GradleException('Verification error')
        }
    }
}
