package com.apphance.ameba.plugins.apphance

import com.apphance.ameba.AbstractPrepareSetupOperation
import com.apphance.ameba.plugins.projectconfiguration.ProjectConfiguration
import com.apphance.ameba.PropertyCategory
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Prepares properties for Apphance integration.
 *
 */
class PrepareApphanceSetupOperation extends AbstractPrepareSetupOperation {

    Logger logger = Logging.getLogger(PrepareApphanceSetupOperation.class)
    ProjectConfiguration conf

    PrepareApphanceSetupOperation() {
        super(ApphanceProperty.class)
    }

    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        BufferedReader br = getReader()
        use(PropertyCategory) {
            ApphanceProperty.each {
                project.getProjectPropertyFromUser(it, null, br)
            }
            appendProperties()
        }
    }
}
