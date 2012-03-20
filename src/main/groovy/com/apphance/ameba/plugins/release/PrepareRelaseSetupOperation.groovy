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
        def files = getFiles { it.name.toLowerCase().equals('icon.png') }

        BufferedReader br = getReader()
        use (PropertyCategory) {
            ProjectReleaseProperty.each {
                if (it == ProjectReleaseProperty.RELEASE_PROJECT_ICON_FILE) {
                    project.getProjectPropertyFromUser(it, files, br)
                } else {
                    project.getProjectPropertyFromUser(it, null, br)
                }
            }
            appendProperties()
        }
    }
}
