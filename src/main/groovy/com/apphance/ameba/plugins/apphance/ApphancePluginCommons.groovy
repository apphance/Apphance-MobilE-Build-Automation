package com.apphance.ameba.plugins.apphance

import com.apphance.ameba.PropertyCategory
import org.gradle.api.Project

class ApphancePluginCommons {

    def prepareApphanceLibDependency(Project p, String defaultDependency) {
        boolean apphanceDependencyNotPresent = p.configurations.apphance.dependencies.isEmpty()
        if (apphanceDependencyNotPresent) {
            String apphanceLibDependency = ''
            use(PropertyCategory) {
                apphanceLibDependency = p.readPropertyOrEnvironmentVariable('apphance.lib', true)?.trim()
                apphanceLibDependency = apphanceLibDependency ?: defaultDependency
                p.dependencies {
                    apphance apphanceLibDependency
                }
            }
        }
        def dependency = (p.configurations.apphance.dependencies as List)[0]
        return [dependency.group, dependency.name, dependency.version].join(':')
    }

    def addApphanceConfiguration(Project project) {
        project.configurations {
            apphance
        }

        project.configurations.apphance {
            resolutionStrategy.cacheDynamicVersionsFor 0, 'minutes'
        }

        project.repositories {
            maven { url 'https://dev.polidea.pl/artifactory/libs-releases-local/' }
            maven { url 'https://dev.polidea.pl/artifactory/libs-snapshots-local/' }
        }
    }
}
