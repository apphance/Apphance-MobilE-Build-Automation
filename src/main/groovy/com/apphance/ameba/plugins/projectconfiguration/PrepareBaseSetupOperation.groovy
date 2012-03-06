package com.apphance.ameba.plugins.projectconfiguration

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AbstractPrepareSetupOperation;
import com.apphance.ameba.PropertyCategory


class PrepareBaseSetupOperation extends AbstractPrepareSetupOperation {
    Logger logger = Logging.getLogger(PrepareBaseSetupOperation.class)

    PrepareBaseSetupOperation() {
        super(BaseProperty.class)
    }

    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        def files = getFiles { it.name.toLowerCase().equals('icon.png') }
        BufferedReader br = getReader()
        use(PropertyCategory) {
            BaseProperty.each {
                if (it == BaseProperty.PROJECT_ICON_FILE) {
                    project.getProjectPropertyFromUser(it, files, br)
                } else {
                    project.getProjectPropertyFromUser(it, null, br)
                }
            }
            appendProperties()
        }
    }
}
