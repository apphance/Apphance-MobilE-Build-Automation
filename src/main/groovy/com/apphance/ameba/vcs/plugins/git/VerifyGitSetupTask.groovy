package com.apphance.ameba.vcs.plugins.git

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractVerifySetupTask

class VerifyGitSetupTask extends  AbstractVerifySetupTask {
    Logger logger = Logging.getLogger(VerifyGitSetupTask.class)

    VerifyGitSetupTask() {
        super(GitProperty.class)
    }

    @TaskAction
    void verifySetup() {
        def projectProperties = readProperties()
        GitProperty.each { checkProperty(projectProperties, it) }
        allPropertiesOK()
    }
}
