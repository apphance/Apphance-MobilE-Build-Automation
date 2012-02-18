package com.apphance.ameba.ios

import org.gradle.api.GradleException
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import com.apphance.ameba.AbstractVerifySetupTask
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSProjectConfiguration;
import com.apphance.ameba.ios.IOSXCodeOutputParser


abstract class AbstractVerifyIOSSetupTask extends AbstractVerifySetupTask {
    Logger logger = Logging.getLogger(AbstractVerifyIOSSetupTask.class)

    IOSXCodeOutputParser iosXCodeOutputParser
    IOSProjectConfiguration iosConf

    AbstractVerifyIOSSetupTask(Class<? extends Enum> clazz) {
        super(clazz)
        iosXCodeOutputParser = new IOSXCodeOutputParser()
        iosConf = iosXCodeOutputParser.getIosProjectConfiguration(project)
        this.dependsOn(project.verifyBaseSetup)
    }

    void checkPlistFile(property) {
        use (PropertyCategory) {
            File plistFile = new File(project.rootDir,project.readExpectedProperty(property))
            if (!plistFile.exists() || !plistFile.isFile()) {
                throw new GradleException("""The plist file ${property.propertyName}:${plistFile}) does not exist or is not a file. Please run 'gradle prepareSetup' to correct it.""")
            }
        }
    }

    void checkBuildableTarget(property) {
        use (PropertyCategory) {
            String target = project.readProperty(property)
            if (!iosConf.targets.contains(target)) {
                throw new GradleException("""The target in ${property.propertyName}: ${target} can only be one of ${iosConf.targets}""")
            }
        }
    }

    void checkBuildableConfiguration(property) {
        use (PropertyCategory) {
            String configuration = project.readProperty(property)
            if (!iosConf.configurations.contains(configuration)) {
                throw new GradleException("""The configuration in ${property.propertyName}: ${configuration} can only be one of ${iosConf.configurations}""")
            }
        }
    }
    
    void checkTarget(property) {
        use (PropertyCategory) {
            String target = project.readProperty(property)
            if (!iosConf.alltargets.contains(target)) {
                throw new GradleException("""The target in ${property.propertyName}: ${target} can only be one of ${iosConf.alltargets}""")
            }
        }
    }

    void checkConfiguration(property) {
        use (PropertyCategory) {
            String configuration = project.readProperty(property)
            if (!iosConf.allconfigurations.contains(configuration)) {
                throw new GradleException("""The configuration in ${property.propertyName}: ${configuration} can only be one of ${iosConf.allconfigurations}""")
            }
        }
    }
}
