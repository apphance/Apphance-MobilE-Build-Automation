package com.apphance.flow.plugins.project.tasks

import com.apphance.flow.configuration.AbstractConfiguration
import groovy.transform.PackageScope
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_SETUP

class VerifySetupTask extends DefaultTask {

    static String NAME = 'verifySetup'
    String description = "Runs validation over all required properties in flow.properties. " +
            "It's executed before every build."
    String group = FLOW_SETUP

    @Inject Map<Integer, AbstractConfiguration> configurations

    @TaskAction
    void verifySetup() {
        List errors = []
        verifyConfigurations(configurations.sort().values(), errors)
        if (errors) {
            errors.each { logger.error("ERROR: $it") }
            throw new GradleException('Verification error')
        }
    }

    @PackageScope
    void verifyConfigurations(Collection<? extends AbstractConfiguration> confs, List<String> errors) {
        confs.each {
            if (it.isEnabled()) {
                errors.addAll(it.verify())
                verifyConfigurations(it.subConfigurations, errors)
            }
        }
    }
}
