package com.apphance.ameba

enum ProjectBaseProperty {
	
        PROJECT_NAME(false, 'project.name', 'Name of the project'),
        PROJECT_ICON_FILE(false, "project.icon.file", 'Path to project icon file'),
        PROJECT_URL(false, 'project.url.base', 'Project url'),
        PROJECT_DIRECTORY(false, 'project.directory.name', 'Name of project directory'),
        PROJECT_LANGUAGE(false, 'project.language', 'Language of project'),
        PROJECT_COUNTRY(false, 'project.country', 'Project country');
		
		private final boolean optional
		private final String name
		private final String description
		
		ProjectBaseProperty(boolean optional, String name, String description) {
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

}
