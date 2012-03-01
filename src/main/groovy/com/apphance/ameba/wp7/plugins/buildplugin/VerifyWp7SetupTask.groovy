package com.apphance.ameba.wp7.plugins.buildplugin

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractVerifySetupTask

class VerifyWp7SetupTask extends AbstractVerifySetupTask {
	Logger logger = Logging.getLogger(VerifyWp7SetupTask.class)

	VerifyWp7SetupTask() {
		super(Wp7ProjectProperty.class)
	}

	@TaskAction
	void verifySetup() {
	}
}
