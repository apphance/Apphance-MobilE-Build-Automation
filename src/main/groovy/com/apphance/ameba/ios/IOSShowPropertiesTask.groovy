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
		System.out.println("""###########################################################
# IOS properties properties
###########################################################""")
		for (IOSProjectProperty property : IOSProjectProperty.values()) {
			String comment = '# ' + property.getDescription()
			String propString = property.getName() + '='
			if (property.isOptional()) {
				comment = comment + ' [optional]'
			} else {
				comment = comment + ' [required]'
			}
			if (project.hasProperty(property.getName())) {
				propString = propString +  project[property.getName()]
			}
			if (project.showProperties.showComments == true) {
				System.out.println(comment)
			}
			System.out.println(propString)
		}
	}
}
