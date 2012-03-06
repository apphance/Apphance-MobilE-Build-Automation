package com.apphance.ameba.wp7.plugins.buildplugin

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AbstractPrepareSetupOperation
import com.apphance.ameba.PropertyCategory


class PrepareWp7SetupOperation extends AbstractPrepareSetupOperation {
	Logger logger = Logging.getLogger(PrepareWp7SetupOperation.class)

	PrepareWp7SetupOperation() {
		super(Wp7ProjectProperty.class)
	}

    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        BufferedReader br = getReader()
        use (PropertyCategory) {
            Wp7ProjectProperty.each {
                project.getProjectPropertyFromUser(it, null, br)
            }
            appendProperties()
        }
    }
}
