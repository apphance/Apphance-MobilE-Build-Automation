package com.apphance.ameba

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction;

import com.sun.media.jai.codecimpl.fpx.Property;

class ShowBasePropertiesTask extends DefaultTask {

	ShowBasePropertiesTask() {
		this.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
		this.description = 'Prints all base project properties'
		//inject myself as dependency for umbrella verifySetup
		project.showProperties.dependsOn(this)
		this.dependsOn(project.readProjectConfiguration)
	}

	
	@TaskAction
	void showProperties() {
		System.out.println("""###########################################################
# Project properties
###########################################################""")
		for (ProjectBaseProperty property : ProjectBaseProperty.values()) {
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
