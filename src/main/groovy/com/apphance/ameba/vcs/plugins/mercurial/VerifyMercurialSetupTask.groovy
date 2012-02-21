package com.apphance.ameba.vcs.plugins.mercurial

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractVerifySetupTask

class VerifyMercurialSetupTask extends  AbstractVerifySetupTask {
    Logger logger = Logging.getLogger(VerifyMercurialSetupTask.class)

    VerifyMercurialSetupTask() {
        super(MercurialProperty.class)
        this.dependsOn(project.verifyBaseSetup)
    }

    @TaskAction
    void verifySetup() {
        def projectProperties = readProperties()
        MercurialProperty.each { checkProperty(projectProperties, it) }
        allPropertiesOK()
    }
}
