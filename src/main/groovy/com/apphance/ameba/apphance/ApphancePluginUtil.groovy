package com.apphance.ameba.apphance

import com.apphance.ameba.PropertyCategory
import org.gradle.api.Project

class ApphancePluginUtil {

    def prepareApphanceLibDependency(Project p, String defaultDependency) {
        String apphanceLibDependency = ''
        use(PropertyCategory) {
            apphanceLibDependency = p.readPropertyOrEnvironmentVariable('apphance.lib', true)?.trim()
            apphanceLibDependency = apphanceLibDependency ?: defaultDependency
            p.dependencies {
                apphance apphanceLibDependency
            }
        }
        apphanceLibDependency
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
