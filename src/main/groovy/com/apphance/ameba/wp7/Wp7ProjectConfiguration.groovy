package com.apphance.ameba.wp7

import java.util.Collection
import java.util.List;

import org.gradle.api.Project

class Wp7ProjectConfiguration {

	List<String> targets = []
	List<String> configurations = []

	public File getVariantDirectory(Project project, String target, String configuration) {
		return new File(project.rootDir.parent, project.name+"-"+target+"-"+configuration)
	}

	@Override
	public String toString() {
		return this.getProperties()
	}
}
