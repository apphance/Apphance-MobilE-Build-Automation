package com.apphance.ameba.ios
import org.gradle.api.tasks.TaskAction
import com.apphance.ameba.AmebaCommonBuildTaskGroups

import org.gradle.api.DefaultTask

class IOSShowPropertiesTask extends DefaultTask {
	IOSShowPropertiesTask() {
		this.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
		this.description = 'Prints all ios project properties'
		//inject myself as dependency for umbrella verifySetup
		project.showProperties.dependsOn(this)
		this.dependsOn(project.readProjectConfiguration)
	}

	
	@TaskAction
	void showProperties() {
		System.out.println(IOSProjectProperty.printProperties(project, true))
	}
}
