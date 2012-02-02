package com.apphance.ameba

import java.util.Properties;

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction
import org.gradle.api.logging.Logging


class VerifyReleaseSetupTask extends DefaultTask {

	Logger logger = Logging.getLogger(VerifyBaseSetupTask.class)
	
    VerifyReleaseSetupTask() {
        this.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        this.description = 'Verifies if release properties of the project have been setup properly'
        //inject myself as dependency for umbrella verifySetup
        project.verifySetup.dependsOn(this)
    }
	
	@TaskAction
	void verifySetup() {
		projectProperties = new Properties()
		def projectPropertiesFile = new File(project.rootDir,'gradle.properties')
		if (!projectPropertiesFile.exists()) {
			throw new GradleException("""The gradle.properties file does not exist.
!!!!! Please run "gradle prepareSetup" to correct project's configuration !!!!!""")
		}
		projectProperties.load(projectPropertiesFile.newInputStream())
		logger.lifecycle(projectProperties.toString())
		for (ProjectReleaseProperty property : ProjectReleaseProperty.values()) {
			if (!property.isOptional()) {
				checkProperty(projectProperties, property.getName())
			}
		}
		logger.lifecycle("GOOD!!! ALL PROJECT PROPERTIES SET CORRECTLY!!!")
	}
	
	void checkProperty(Properties projectProperties, String propertyName) {
		if (projectProperties.getProperty(propertyName) == null) {
			throw new GradleException("""Property ${propertyName} should be defined in gradle.properties.
!!!!! Please run "gradle prepareSetup" to correct it """)
		}
	}
}
