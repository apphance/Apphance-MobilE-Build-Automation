package com.apphance.ameba.wp7.plugins.buildplugin

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AbstractVerifySetupOperation



class VerifyWp7SetupOperation extends AbstractVerifySetupOperation {
	Logger logger = Logging.getLogger(VerifyWp7SetupOperation.class)

	VerifyWp7SetupOperation() {
		super(Wp7ProjectProperty.class)
	}


	void verifySetup() {
	}
}
