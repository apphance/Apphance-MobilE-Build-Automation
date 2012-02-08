package com.apphance.ameba.ios

import org.gradle.api.Project

enum IOSProjectProperty {

	PLIST_FILE(false, 'ios.plist.file', 'Path to plist file'),
	EXCLUDED_BUILDS(false, 'ios.excluded.builds', 'List of excluded builds'),
	IOS_FAMILIES(false, 'ios.families', 'List of iOS families'),
	DISTRIBUTION_DIR(false, 'ios.distribution.resources.dir', 'Path to distribution resources directory'),
	MAIN_TARGET(true, 'ios.mainTarget', 'Main target for release build'),
	MAIN_CONFIGURATION(true, 'ios.mainConfiguration', 'Main configuration for release build'),
	IOS_SDK(true, 'ios.sdk', 'List of iOS SDKs'),
	IOS_SIMULATOR_SDK(true, 'ios.simulator.sdk', 'List of iOS simulator SDKs'),
	FONE_MONKEY_CONFIGURATION(true, 'ios.fonemonkey.configuration', 'FoneMonkey build configuration'),
	KIF_CONFIGURATION(true, 'ios.kif.configuration', 'KIF build configuration');

	private final boolean optional
	private final String name
	private final String description

	IOSProjectProperty(boolean optional, String name, String description) {
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
		String s
		s = """###########################################################
# IOS properties properties
###########################################################\n"""
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
			if (useComments == true) {
				s = s + comment
			}
			s = s + propString + '\n'
		}
		return s
	}
}
