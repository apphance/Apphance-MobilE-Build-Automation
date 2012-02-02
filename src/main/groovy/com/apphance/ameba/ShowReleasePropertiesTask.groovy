package com.apphance.ameba

import org.gradle.api.tasks.TaskAction
import org.gradle.api.DefaultTask

class ShowReleasePropertiesTask extends DefaultTask {
	
	ShowReleasePropertiesTask() {
		this.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
		this.description = 'Prints all release mail project properties'
		//inject myself as dependency for umbrella verifySetup
		project.showProperties.dependsOn(this)
		this.dependsOn(project.readProjectConfiguration)
	}

	
	@TaskAction
	void showProperties() {
		System.out.println("""###########################################################
# Release mail properties
###########################################################""")
		for (ProjectReleaseProperty property : ProjectReleaseProperty.values()) {
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
			System.out.println(comment)
			System.out.println(propString)
		}
	}
}
