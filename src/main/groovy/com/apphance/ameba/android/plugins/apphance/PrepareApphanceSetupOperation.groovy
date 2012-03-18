package com.apphance.ameba.android.plugins.apphance

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AbstractPrepareSetupOperation
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.PropertyCategory

class PrepareApphanceSetupOperation extends AbstractPrepareSetupOperation {

    Logger logger = Logging.getLogger(PrepareApphanceSetupOperation.class)
    ProjectConfiguration conf

    PrepareApphanceSetupOperation() {
        super(AndroidApphanceProperty.class)
    }

    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        BufferedReader br = getReader()
        use (PropertyCategory) {
            AndroidApphanceProperty.each {
                project.getProjectPropertyFromUser(it, null, br)
            }
            appendProperties()
        }
    }
}
