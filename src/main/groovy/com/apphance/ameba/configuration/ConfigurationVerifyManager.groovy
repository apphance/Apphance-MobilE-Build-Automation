package com.apphance.ameba.configuration

import org.gradle.api.GradleException

@com.google.inject.Singleton
class ConfigurationVerifyManager {

    private List<AbstractConfiguration> configurationsForVerification = []

    void registerConfiguration(AbstractConfiguration configuration) {
        configurationsForVerification << configuration
    }

    List<String> verify() {
        List errors = []
        configurationsForVerification.each {
            it.verify()
            errors += it.errors
        }

        if (errors) {
            errors.each {
                println "ERROR: $it"
            }
            throw new GradleException('Verification error')
        }
    }
}
