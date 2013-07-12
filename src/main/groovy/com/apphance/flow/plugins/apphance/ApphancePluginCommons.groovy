package com.apphance.flow.plugins.apphance

import org.gradle.api.Project

class ApphancePluginCommons {

    def addApphanceConfiguration(Project project, String confName) {
        project.configurations.create(confName)
        project.configurations.getByName(confName) {
            resolutionStrategy.cacheDynamicVersionsFor 0, 'minutes'
        }

        project.repositories {
            maven { url 'https://dev.polidea.pl/artifactory/libs-releases-local/' }
            maven { url 'https://dev.polidea.pl/artifactory/libs-snapshots-local/' }
        }
    }
}
