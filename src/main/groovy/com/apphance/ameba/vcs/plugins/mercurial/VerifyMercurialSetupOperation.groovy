package com.apphance.ameba.vcs.plugins.mercurial

import com.apphance.ameba.AbstractVerifySetupOperation
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Verifies Mercurial-related properties.
 *
 */
class VerifyMercurialSetupOperation extends AbstractVerifySetupOperation {
    Logger logger = Logging.getLogger(VerifyMercurialSetupOperation.class)

    VerifyMercurialSetupOperation() {
        super(MercurialProperty.class)
    }


    void verifySetup() {
        def projectProperties = readProperties()
        MercurialProperty.each { checkProperty(projectProperties, it) }
        allPropertiesOK()
    }
}
