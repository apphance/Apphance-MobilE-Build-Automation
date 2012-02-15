package com.apphance.ameba.apphance

import org.gradle.api.tasks.TaskAction;
import com.apphance.ameba.PropertyCategory;
import com.apphance.ameba.AbstractVerifySetupTask

class VerifyApphanceSetupTask extends AbstractVerifySetupTask {
	
	VerifyApphanceSetupTask() {
		super(ApphanceProperty.class)
	}
	
	@TaskAction
	void verifySetup() {
		use (PropertyCategory) {
			def projectProperties = readProperties()
			ApphanceProperty.each {
				checkProperty(projectProperties, it)
			}
			allPropertiesOK()
		}
	}
}
