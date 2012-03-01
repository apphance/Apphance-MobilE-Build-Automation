package com.apphance.ameba.unit.wp7

import static org.junit.Assert.*

import java.io.File

import org.junit.Before
import org.junit.Test

import com.apphance.ameba.wp7.CsprojHelper

// Add verification similar AndroidManifestHelperTest
class CsprojHelperTest {


	String projectDir = "testProjects/wp7/AmebaTest/"
	CsprojHelper csprojHelper
	File tmpDir


	@Before
	void setUp() {
		this.csprojHelper = new CsprojHelper()
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
