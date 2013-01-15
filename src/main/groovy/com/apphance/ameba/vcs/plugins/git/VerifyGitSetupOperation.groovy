package com.apphance.ameba.vcs.plugins.git

import com.apphance.ameba.AbstractVerifySetupOperation
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Verifies all git-related properties.
 *
 */
class VerifyGitSetupOperation extends AbstractVerifySetupOperation {
    Logger logger = Logging.getLogger(VerifyGitSetupOperation.class)

    VerifyGitSetupOperation() {
        super(GitProperty.class)
    }


    void verifySetup() {
        def projectProperties = readProperties()
        GitProperty.each { checkProperty(projectProperties, it) }
        allPropertiesOK()
    }
}
