package com.apphance.flow.validation

import com.apphance.flow.configuration.AbstractConfiguration
import groovy.transform.PackageScope
import org.gradle.api.GradleException

import static org.apache.commons.lang.StringUtils.isNotEmpty
import static org.gradle.api.logging.Logging.getLogger

//TODO should be tested
class ConfigurationValidator {

    private logger = getLogger(getClass())

    void validate(Collection<? extends AbstractConfiguration> configurations) {
        List<String> errors = []
        validateConfigurations(configurations, errors)
        errors = errors.findAll { isNotEmpty(it) }
        if (errors) {
            errors.each { logger.error("ERROR: $it") }
            throw new GradleException('Validation error')
        }
    }

    @PackageScope
    void validateConfigurations(Collection<? extends AbstractConfiguration> confs, List<String> errors) {
        confs.findAll { it.isEnabled() }.each {
            it.validate(errors)
            validateConfigurations(it.subConfigurations, errors)
        }
    }
}
