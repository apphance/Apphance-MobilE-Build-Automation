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
			try {
				major =  Integer.parseInt(matcher[0][1])
				minor =  Integer.parseInt(matcher[0][2])
				build =  Integer.parseInt(matcher[0][3])
				revision = Integer.parseInt(matcher[0][4])
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		else {

		}


	}

	@Override
	public String toString() {
		return major+"."+minor+"."+build+"."+revision
	}



}
