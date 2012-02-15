package com.apphance.ameba.vcs.plugins.git

import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractPrepareSetupTask;
import com.apphance.ameba.PropertyCategory;


class PrepareGitSetupTask extends AbstractPrepareSetupTask {
    Logger logger = Logging.getLogger(PrepareGitSetupTask.class)

    PrepareGitSetupTask() {
        super(GitProperty.class)
        this.dependsOn(project.prepareBaseSetup)
    }

    @TaskAction
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
