package com.apphance.ameba.plugins.release

import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractPrepareSetupTask;
import com.apphance.ameba.ProjectConfiguration;
import com.apphance.ameba.PropertyCategory;

class PrepareReleaseSetupTask extends AbstractPrepareSetupTask {

    Logger logger = Logging.getLogger(PrepareReleaseSetupTask.class)
    ProjectConfiguration conf

    PrepareReleaseSetupTask() {
        super(ProjectReleaseProperty.class)
        this.dependsOn(project.prepareBaseSetup)
    }

    @TaskAction
    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        BufferedReader br = getReader()
        use (PropertyCategory) {
            ProjectReleaseProperty.each {
                project.getProjectPropertyFromUser(it, null, br)
            }
            appendProperties()
        }
    }
}
