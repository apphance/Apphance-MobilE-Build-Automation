package com.apphance.ameba.apphance

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction;

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.PropertyCategory



import com.apphance.ameba.AbstractPrepareSetupTask

class PrepareApphanceSetup extends AbstractPrepareSetupTask {

	Logger logger = Logging.getLogger(PrepareApphanceSetup.class)
	ProjectConfiguration conf

	PrepareApphanceSetup() {
		super(ApphanceProperty.class)
	}

	@TaskAction
	void prepareSetup() {
		logger.lifecycle("Preparing ${propertyDescription}")
		BufferedReader br = getReader()
		use (PropertyCategory) {
			ApphanceProperty.each {
				project.getProjectPropertyFromUser(it, null, br)
			}
			appendProperties()
		}
	}
}
