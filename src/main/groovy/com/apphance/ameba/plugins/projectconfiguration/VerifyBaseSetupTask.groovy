package com.apphance.ameba.plugins.projectconfiguration

import org.gradle.api.GradleException
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractVerifySetupTask
import com.apphance.ameba.PropertyCategory

class VerifyBaseSetupTask extends  AbstractVerifySetupTask<ProjectBaseProperty> {
    Logger logger = Logging.getLogger(VerifyBaseSetupTask.class)

    VerifyBaseSetupTask() {
        super(ProjectBaseProperty.class)
    }

    @TaskAction
    void verifySetup() {
        def projectProperties = readProperties()
        ProjectBaseProperty.each { checkProperty(projectProperties, it) }
        checkIconFile(projectProperties)
        allPropertiesOK()
    }

    void checkIconFile(Properties projectProperties) {
        use (PropertyCategory) {
            File iconFile = new File(project.rootDir,project.readProperty(ProjectBaseProperty.PROJECT_ICON_FILE))
            if (!iconFile.exists() || !iconFile.isFile()) {
                throw new GradleException("""The icon file (${iconFile}) does not exist
    or is not a file. Please run 'gradle prepareSetup' to correct it.""")
            }
        }
    }
}
