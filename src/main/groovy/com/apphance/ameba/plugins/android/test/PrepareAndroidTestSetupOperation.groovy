package com.apphance.ameba.plugins.android.test

import com.apphance.ameba.AbstractPrepareSetupOperation
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.plugins.android.AndroidProjectConfigurationRetriever
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Prepares configuration for android tests.
 *
 */
class PrepareAndroidTestSetupOperation extends AbstractPrepareSetupOperation {
    Logger logger = Logging.getLogger(PrepareAndroidTestSetupOperation.class)
    List BOOLEANS = ['true', 'false']

    PrepareAndroidTestSetupOperation() {
        super(AndroidTestProperty.class)
    }

    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        use(PropertyCategory) {
            BufferedReader br = getReader()
            AndroidTestProperty.each {
                switch (it) {
                    case AndroidTestProperty.EMULATOR_SNAPSHOT_ENABLED:
                    case AndroidTestProperty.EMULATOR_NO_WINDOW:
                    case AndroidTestProperty.TEST_PER_PACKAGE:
                    case AndroidTestProperty.USE_EMMA:
                        project.getProjectPropertyFromUser(it, BOOLEANS, br)
                        break;
                    case AndroidTestProperty.EMULATOR_TARGET:
                        def androidConf = AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration(project)
                        List targets = androidConf.availableTargets
                        project.getProjectPropertyFromUser(it, targets, br)
                        break;
                    default:
                        project.getProjectPropertyFromUser(it, null, br)
                }
            }
            appendProperties()
        }
    }
}