package com.apphance.flow.validation

import com.apphance.flow.configuration.AbstractConfiguration
import groovy.transform.PackageScope
import org.gradle.api.GradleException

import static org.gradle.api.logging.Logging.getLogger

//TODO should be tested
class ConfigurationValidator {

    private logger = getLogger(getClass())

    void validate(Collection<? extends AbstractConfiguration> configurations) {
        List errors = []
        verifyConfigurations(configurations, errors)
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
