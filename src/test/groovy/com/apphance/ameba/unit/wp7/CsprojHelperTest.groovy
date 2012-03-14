package com.apphance.ameba.unit.wp7

import static org.junit.Assert.*

import java.io.File

import org.junit.Before
import org.junit.Test

import com.apphance.ameba.wp7.Wp7ProjectHelper

// Add verification similar AndroidManifestHelperTest
class CsprojHelperTest {


	String projectDir = "testProjects/wp7/AmebaTest/"
	Wp7ProjectHelper csprojHelper
	File tmpDir


	@Before
	void setUp() {
		this.csprojHelper = new Wp7ProjectHelper()
		this.tmpDir = new File("tmp")
		tmpDir.mkdir()
		def androidManifest = new File(tmpDir,"AmebaTest.csproj")
		def originalAndroidManifest = new File(projectDir+"AmebaTest.csproj")
		androidManifest.delete()
		androidManifest << originalAndroidManifest.text
	}

	@Test
	void testGetCsprojName() {
		def csFileName = csprojHelper.getCsprojName(tmpDir);
		assertNotNull(csFileName);
		assertEquals("AmebaTest.csproj", csFileName)
	}

}
