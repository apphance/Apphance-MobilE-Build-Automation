package com.apphance.ameba.vcs.plugins.git

import com.apphance.ameba.AbstractPrepareSetupOperation
import com.apphance.ameba.PropertyCategory
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Prepares Git-related properties.
 *
 */
class PrepareGitSetupOperation extends AbstractPrepareSetupOperation {
    Logger logger = Logging.getLogger(PrepareGitSetupOperation.class)

    PrepareGitSetupOperation() {
        super(GitProperty.class)
    }


    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        BufferedReader br = getReader()
        use(PropertyCategory) {
            GitProperty.each {
                project.getProjectPropertyFromUser(it, null, br)
            }
            appendProperties()
        }
    }
}
