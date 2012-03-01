package com.apphance.ameba.wp7.plugins.buildplugin

public enum Wp7ProjectProperty {

	APPHANCE_APPLICATION_KEY('wp7.apphance.key', 'Apphance Application Key'),
	TEST_DIRECTORY('wp7.test.directory', 'Relative directory of test project'),

	public static final DESCRIPTION = 'Windows Phone project properties'
	final String propertyName
	final String description
	final String defaultValue

	Wp7ProjectProperty(String propertyName, String description, String defaultValue = null) {
		this.propertyName = propertyName
		this.description = description
		this.defaultValue = defaultValue
	}
}
