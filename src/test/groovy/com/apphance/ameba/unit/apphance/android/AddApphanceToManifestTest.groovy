package com.apphance.ameba.unit.apphance.android

import static org.junit.Assert.*
import groovy.util.slurpersupport.GPathResult

import org.junit.*

import com.apphance.ameba.android.AndroidManifestHelper;

class AddApphanceToManifestTest {

	File projectDirectory = new File('testProjects/android-novariants')

	@Test
	void addApphanceTest() {
		File androidManifest = new File(projectDirectory, 'AndroidManifest.xml')
		File copyManifest = new File(projectDirectory, 'AndroidManifest.xml.orig')
		copyManifest.delete()
		copyManifest << androidManifest.text
		try {
			AndroidManifestHelper helper = new AndroidManifestHelper()
			helper.addApphanceToManifest(projectDirectory)
			XmlSlurper slurper = new XmlSlurper()
			GPathResult manifest = slurper.parse(androidManifest)
			def getTasks = manifest."uses-permission".findAll {
				it.@'android:name'.equals("android.permission.GET_TASKS")
			}
			assertEquals(1, getTasks.size())

			def readPhone = manifest."uses-permission".findAll {
				it.@'android:name'.equals("android.permission.READ_PHONE_STATE")
			}
			assertEquals(1, readPhone.size())
		} finally {
			androidManifest.delete()
			androidManifest << copyManifest.text
		}
	}

}
