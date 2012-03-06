package com.apphance.ameba.plugins.release

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging

import com.apphance.ameba.AbstractPrepareSetupOperation;
import com.apphance.ameba.ProjectConfiguration;
import com.apphance.ameba.PropertyCategory;

class PrepareReleaseSetupOperation extends AbstractPrepareSetupOperation {

    Logger logger = Logging.getLogger(PrepareReleaseSetupOperation.class)
    ProjectConfiguration conf

    PrepareReleaseSetupOperation() {
        super(ProjectReleaseProperty.class)
    }

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
