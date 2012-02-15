package com.apphance.ameba.vcs.plugins.mercurial

import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractPrepareSetupTask;
import com.apphance.ameba.PropertyCategory;


class PrepareMercurialSetupTask extends AbstractPrepareSetupTask {
    Logger logger = Logging.getLogger(PrepareMercurialSetupTask.class)

    PrepareMercurialSetupTask() {
        super(MercurialProperty.class)
    }

    @TaskAction
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
