package com.apphance.ameba.plugins.android.jarlibrary

import com.apphance.ameba.AbstractPrepareSetupOperation
import com.apphance.ameba.PropertyCategory
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

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
        use(PropertyCategory) {
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
