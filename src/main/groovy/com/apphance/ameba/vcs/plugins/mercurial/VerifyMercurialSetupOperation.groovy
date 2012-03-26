package com.apphance.ameba.vcs.plugins.mercurial

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import com.apphance.ameba.AbstractVerifySetupOperation

/**
 * Verifies Mercurial-related properties.
 *
 */
class VerifyMercurialSetupOperation extends  AbstractVerifySetupOperation {
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
