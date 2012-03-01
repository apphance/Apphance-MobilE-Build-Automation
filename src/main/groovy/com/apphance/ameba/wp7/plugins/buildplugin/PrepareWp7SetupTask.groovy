package com.apphance.ameba.wp7.plugins.buildplugin

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AbstractPrepareSetupTask

class PrepareWp7SetupTask extends AbstractPrepareSetupTask {
	Logger logger = Logging.getLogger(PrepareWp7SetupTask.class)

	PrepareWp7SetupTask() {
		super(Wp7ProjectProperty.class)
	}
}
