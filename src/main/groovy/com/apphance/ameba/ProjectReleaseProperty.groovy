package com.apphance.ameba

import org.gradle.api.Project;

enum ProjectReleaseProperty {

	RELEASE_MAIL_FROM(false, 'release.mail.from', 'Sender email address'),
	RELEASE_MAIL_TO(false, 'release.mail.to', 'Recipient of release email'),
	RELEASE_MAIL_FLAGS(false, 'release.mail.flags', 'Flags for release email');
	
	private final boolean optional
	private final String name
	private final String description

	ProjectReleaseProperty(boolean optional, String name, String description) {
		this.optional = optional
		this.name = name
		this.description = description
	}

	public boolean isOptional() {
		return optional
	}

	public String getName() {
		return name
	}

	public String getDescription() {
		return description
	}
	
			public static String printProperties(Project project, boolean useComments) {
			
			String s = """###########################################################
# Project release properties
###########################################################\n"""
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
	
				if (useComments == true) {
					s += comment + '\n'
				}
				s += propString + '\n'
			}
			return s
		}
}
