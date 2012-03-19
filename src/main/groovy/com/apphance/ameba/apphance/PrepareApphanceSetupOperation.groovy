package com.apphance.ameba.apphance

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AbstractPrepareSetupOperation
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.apphance.ApphanceProperty;

class PrepareApphanceSetupOperation extends AbstractPrepareSetupOperation {

    Logger logger = Logging.getLogger(PrepareApphanceSetupOperation.class)
    ProjectConfiguration conf

    PrepareApphanceSetupOperation() {
        super(ApphanceProperty.class)
    }

    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        BufferedReader br = getReader()
        use (PropertyCategory) {
            ApphanceProperty.each {
                project.getProjectPropertyFromUser(it, null, br)
            }
            appendProperties()
        }
    }
}
