package com.apphance.ameba.plugins.ios.framework

import com.apphance.ameba.AbstractPrepareSetupOperation
import com.apphance.ameba.plugins.projectconfiguration.ProjectConfiguration
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.plugins.ios.IOSProjectConfiguration
import com.apphance.ameba.plugins.ios.buildplugin.IOSPlugin
import com.apphance.ameba.util.file.FileManager
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Prepares properties for framework preparation.
 *
 */
class PrepareFrameworkSetupOperation extends AbstractPrepareSetupOperation {

    Logger logger = Logging.getLogger(PrepareFrameworkSetupOperation.class)
    ProjectConfiguration conf

    PrepareFrameworkSetupOperation() {
        super(IOSFrameworkProperty.class)
    }

    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        BufferedReader br = getReader()
        def headerFiles = FileManager.getFiles(project, { it.name.endsWith('.h') })
        def resourceFiles = FileManager.getFiles(project, { it.name.endsWith('.png') })
        use(PropertyCategory) {
            IOSProjectConfiguration iosConf = project.ext.get(IOSPlugin.IOS_PROJECT_CONFIGURATION)
            IOSFrameworkProperty.each {
                switch (it) {
                    case IOSFrameworkProperty.FRAMEWORK_CONFIGURATION:
                        project.getProjectPropertyFromUser(it, iosConf.allConfigurations, br)
                        break
                    case IOSFrameworkProperty.FRAMEWORK_TARGET:
                        project.getProjectPropertyFromUser(it, iosConf.allTargets, br)
                        break
                    case IOSFrameworkProperty.FRAMEWORK_HEADERS:
                        project.getProjectPropertyFromUser(it, headerFiles, br)
                        break
                    case IOSFrameworkProperty.FRAMEWORK_RESOURCES:
                        project.getProjectPropertyFromUser(it, resourceFiles, br)
                        break
                    default:
                        project.getProjectPropertyFromUser(it, null, br)
                }
            }
            appendProperties()
        }
    }
}