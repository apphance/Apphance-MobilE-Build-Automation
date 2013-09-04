package com.apphance.flow.configuration.android

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.properties.FileProperty
import com.google.inject.Singleton

import javax.inject.Inject

@Singleton
class AndroidAnalysisConfiguration extends AbstractConfiguration {

    String configurationName = 'Android Analysis Configuration'
    private boolean enabledInternal = false

    @Inject AndroidConfiguration conf

    @Override
    boolean isEnabled() {
        enabledInternal && conf.isEnabled()
    }

    @Override
    void setEnabled(boolean enabled) {
        enabledInternal = enabled
    }

    def pmdRules = new FileProperty(
            name: 'android.analysis.pmd.rules',
            message: 'Path to pmd rules config file'
    )

    def findbugsExclude = new FileProperty(
            name: 'android.analysis.findbugs.exclude',
            message: 'Path to findbugs exclude file'
    )

    def checkstyleConfigFile = new FileProperty(
            name: 'android.analysis.checkstyle.config',
            message: 'Path to checkstyle config file'
    )

    @Override
    void checkProperties() {
        existsOrNull pmdRules, findbugsExclude, checkstyleConfigFile
    }

    void existsOrNull(FileProperty... fileProperties) {
        fileProperties.each {
            check it.value == null || it.value.exists(), "Incorrect value of '$it.name' property"
        }
    }
}

