package com.apphance.ameba.unit.wp7

import static org.junit.Assert.*

import java.io.File

import org.junit.Before
import org.junit.Test

import com.apphance.ameba.ProjectConfiguration;
import com.apphance.ameba.wp7.Wp7ProjectHelper

// Add verification similar AndroidManifestHelperTest
class Wp7ProjectHelperTest {


	String projectDir = "testProjects/wp7/AmebaTest/"
	Wp7ProjectHelper wp7ProjectHelper
	File tmpDir
	ProjectConfiguration conf

	@Before
	void setUp() {
		this.wp7ProjectHelper = new Wp7ProjectHelper()
		this.tmpDir = new File("tmp")
		tmpDir.mkdir()
		conf = new ProjectConfiguration()

	}

	@Test
	void testGetCsprojName() {

		def androidManifest = new File(tmpDir,"AmebaTest.csproj")
		def originalAndroidManifest = new File(projectDir,"AmebaTest.csproj")
		androidManifest.delete()
		androidManifest << originalAndroidManifest.text

		def csFileName = wp7ProjectHelper.getCsprojName(tmpDir);
		assertNotNull(csFileName);
		assertEquals("AmebaTest.csproj", csFileName)
	}

	@Test
	void testReadVersionFromWMAppManifest() {

		def WMAppManifest = new File(tmpDir,"WMAppManifest.xml")
		def originalWMAppManifest = new File(projectDir,"Properties/WMAppManifest.xml")
		WMAppManifest.delete()
		WMAppManifest << originalWMAppManifest.text

		def csFileName = wp7ProjectHelper.readVersionFromWMAppManifest(WMAppManifest, conf)
		assertNotNull(conf.versionString)
	}



}
