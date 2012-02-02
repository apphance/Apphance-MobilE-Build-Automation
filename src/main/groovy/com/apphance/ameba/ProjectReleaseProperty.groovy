package com.apphance.ameba

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
}
