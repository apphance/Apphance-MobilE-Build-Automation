package com.apphance.ameba.android.plugins.jarlibrary


import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction;

import com.apphance.ameba.AbstractPrepareSetupTask
import com.apphance.ameba.PropertyCategory


class PrepareAndroidJarLibrarySetupTask extends AbstractPrepareSetupTask {
    Logger logger = Logging.getLogger(PrepareAndroidJarLibrarySetupTask.class)

    PrepareAndroidJarLibrarySetupTask() {
        super(AndroidJarLibraryProperty.class)
    }

    @TaskAction
    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        def plistFiles = getPlistFiles()
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
