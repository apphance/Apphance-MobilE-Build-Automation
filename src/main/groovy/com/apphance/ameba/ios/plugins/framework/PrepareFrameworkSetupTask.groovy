package com.apphance.ameba.ios.plugins.framework

import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractPrepareSetupTask;
import com.apphance.ameba.ProjectConfiguration;
import com.apphance.ameba.PropertyCategory;

class PrepareFrameworkSetupTask extends AbstractPrepareSetupTask {

    Logger logger = Logging.getLogger(PrepareFrameworkSetupTask.class)
    ProjectConfiguration conf

    PrepareFrameworkSetupTask() {
        super(IOSFrameworkProperty.class)
    }

    @TaskAction
    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in))
        use (PropertyCategory) {
            IOSFrameworkProperty.each {
                project.getProjectPropertyFromUser(it, null, br)
            }
            appendToGeneratedPropertyString(project.listPropertiesAsString(IOSFrameworkProperty.class, false))
        }
    }
}
