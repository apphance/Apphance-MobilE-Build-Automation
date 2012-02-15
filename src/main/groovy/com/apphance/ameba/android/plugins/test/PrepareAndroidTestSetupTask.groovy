package com.apphance.ameba.android.plugins.test


import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction;

import com.apphance.ameba.AbstractPrepareSetupTask
import com.apphance.ameba.PropertyCategory


class PrepareAndroidTestSetupTask extends AbstractPrepareSetupTask {
    Logger logger = Logging.getLogger(PrepareAndroidTestSetupTask.class)

    PrepareAndroidTestSetupTask() {
        super(AndroidTestProperty.class)
    }

    @TaskAction
    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        def plistFiles = getPlistFiles()
        use (PropertyCategory) {
            BufferedReader br = getReader()
            AndroidTestProperty.each {
                switch (it) {
                    default:
                        project.getProjectPropertyFromUser(it, null, br)
                }
            }
            appendProperties()
        }
    }
}
