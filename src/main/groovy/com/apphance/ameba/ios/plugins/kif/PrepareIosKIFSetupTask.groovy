package com.apphance.ameba.ios.plugins.kif

import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractPrepareSetupTask;
import com.apphance.ameba.ProjectConfiguration;
import com.apphance.ameba.PropertyCategory;

class PrepareIosKIFSetupTask extends AbstractPrepareSetupTask {

    Logger logger = Logging.getLogger(PrepareIosKIFSetupTask.class)
    ProjectConfiguration conf

    PrepareIosKIFSetupTask() {
        super(IOSKifProperty.class)
    }

    @TaskAction
    void prepareSetup() {
        logger.lifecycle('Preparing ${propertyDescription}')
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in))
        use (PropertyCategory) {
            IOSKifProperty.each {
                project.getProjectPropertyFromUser(it, null, false, br)
            }
            appendToGeneratedPropertyString(project.listPropertiesAsString(IOSKifProperty.class, false))
        }
    }
}
