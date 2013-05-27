package com.apphance.ameba.plugins.project.tasks

import com.apphance.ameba.configuration.AbstractConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_SETUP

/**
 * Verifies all properties.
 *
 */
class VerifySetupTask extends DefaultTask {

    static String NAME = 'verifySetup'
    String description = 'Verifies if the project can be build properly'
    String group = AMEBA_SETUP

    @Inject Map<Integer, AbstractConfiguration> configurations

    @TaskAction
    void verifySetup() {
        List errors = []
        configurations.sort().values().findAll { it.isEnabled() }.each {
            it.verify()
            errors += it.errors
        }
        if (errors) {
            errors.each { logger.error("ERROR: $it") }
            throw new GradleException('Verification error')
        }
    }
}
