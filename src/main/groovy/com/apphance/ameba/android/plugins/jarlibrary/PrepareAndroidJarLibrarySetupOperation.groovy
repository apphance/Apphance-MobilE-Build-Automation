package com.apphance.ameba.android.plugins.jarlibrary


import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.Project

import com.apphance.ameba.AbstractPrepareSetupOperation
import com.apphance.ameba.PropertyCategory


/**
 * Sets up android jar library properties.
 *
 */
class PrepareAndroidJarLibrarySetupOperation extends AbstractPrepareSetupOperation {
    Logger logger = Logging.getLogger(PrepareAndroidJarLibrarySetupOperation.class)

    PrepareAndroidJarLibrarySetupOperation() {
        super(AndroidJarLibraryProperty.class)
    }

    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        use (PropertyCategory) {
            BufferedReader br = getReader()
            AndroidJarLibraryProperty.each {
                switch (it) {
                    default:
                        project.getProjectPropertyFromUser(it, null, br)
                }
            }
            appendProperties()
        }
    }
}
