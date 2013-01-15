package com.apphance.ameba.vcs.plugins.mercurial

import com.apphance.ameba.AbstractPrepareSetupOperation
import com.apphance.ameba.PropertyCategory
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Sets up all mercurial-related properties.
 *
 */
class PrepareMercurialSetupOperation extends AbstractPrepareSetupOperation {
    Logger logger = Logging.getLogger(PrepareMercurialSetupOperation.class)

    PrepareMercurialSetupOperation() {
        super(MercurialProperty.class)
    }


    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        BufferedReader br = getReader()
        use(PropertyCategory) {
            MercurialProperty.each {
                project.getProjectPropertyFromUser(it, null, br)
            }
            appendProperties()
        }
    }
}
