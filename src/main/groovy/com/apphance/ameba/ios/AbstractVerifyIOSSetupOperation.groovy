package com.apphance.ameba.ios

import com.apphance.ameba.AbstractVerifySetupOperation
import com.apphance.ameba.PropertyCategory
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Abstract class for verification of all ios-related properties.
 *
 */
abstract class AbstractVerifyIOSSetupOperation extends AbstractVerifySetupOperation {
    Logger logger = Logging.getLogger(AbstractVerifyIOSSetupOperation.class)

    IOSProjectConfiguration iosConf

    AbstractVerifyIOSSetupOperation(Class<? extends Enum> clazz) {
        super(clazz)
    }

    void verifySetup() {
        iosConf = IOSXCodeOutputParser.getIosProjectConfiguration(project)
    }

    void checkPlistFile(property) {
        use(PropertyCategory) {
            File plistFile = project.file(project.readExpectedProperty(property))
            if (!plistFile.exists() || !plistFile.isFile()) {
                throw new GradleException("""The plist file ${property.propertyName}:${plistFile}) does not exist or is not a file. Please run 'gradle prepareSetup' to correct it.""")
            }
        }
    }

    void checkBuildableTarget(property) {
        use(PropertyCategory) {
            String target = project.readProperty(property)
            if (!iosConf.targets.contains(target)) {
                throw new GradleException("""The target in ${property.propertyName}: ${target} can only be one of ${iosConf.targets}""")
            }
        }
    }

    void checkBuildableConfiguration(property) {
        use(PropertyCategory) {
            String configuration = project.readProperty(property)
            if (!iosConf.configurations.contains(configuration)) {
                throw new GradleException("""The configuration in ${property.propertyName}: ${configuration} can only be one of ${iosConf.configurations}""")
            }
        }
    }

    void checkTarget(property) {
        use(PropertyCategory) {
            String target = project.readProperty(property)
            if (!iosConf.allTargets.contains(target)) {
                throw new GradleException("""The target in ${property.propertyName}: ${target} can only be one of ${iosConf.allTargets}""")
            }
        }
    }

    void checkConfiguration(property) {
        use(PropertyCategory) {
            String configuration = project.readProperty(property)
            if (!iosConf.allConfigurations.contains(configuration)) {
                throw new GradleException("""The configuration in ${property.propertyName}: ${configuration} can only be one of ${iosConf.allConfigurations}""")
            }
        }
    }
}
