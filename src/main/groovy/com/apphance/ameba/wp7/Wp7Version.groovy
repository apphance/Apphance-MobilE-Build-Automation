package com.apphance.ameba.wp7

class Wp7Version {

	int major
	int minor
	int build
	int revision

	public static Wp7Version fromString(String versionString) {
		return new Wp7Version(versionString)
	}
	public Wp7Version(String versionString) {
		def versionRegex = /(\d+)\.(\d+)\.(\d+)\.(\d+)/

		def matcher = ( versionString =~ versionRegex )

		if(matcher.matches()) {
			major =  matcher[0][0]
			minor =  matcher[0][1]
			build =  matcher[0][2]
			revision =  matcher[0][3]
		}
		else {

		}


	}

	@Override
	public String toString() {
		return major+"."+minor+"."+build+"."+revision+"."
	}



}
