package com.apphance.ameba.unit.wp7

import static org.junit.Assert.*

import java.io.File

import org.junit.Before
import org.junit.Test

import com.apphance.ameba.ProjectConfiguration;
import com.apphance.ameba.wp7.Wp7ProjectConfiguration;
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
	void testUpdateVersion() {
		def properties = new File(tmpDir,"Properties")
		properties.mkdirs()

		def WMAppManifest = new File(properties,"WMAppManifest.xml")
		def originalWMAppManifest = new File(projectDir,"Properties/WMAppManifest.xml")
		WMAppManifest.delete()

		WMAppManifest << originalWMAppManifest.text
		wp7ProjectHelper.updateVersion(tmpDir, conf)
	}


	@Test
	void testGetCsprojName() {

		def csproj = new File(tmpDir,"AmebaTest.csproj")
		def originaCsproj = new File(projectDir,"AmebaTest.csproj")
		csproj.delete()
		csproj << originaCsproj.text

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

	@Test
	void testReadConfigurationsFromSln() {
		File slnFile = wp7ProjectHelper.getSolutionFile(new File(projectDir));
		Wp7ProjectConfiguration wp7conf = new Wp7ProjectConfiguration();
		wp7ProjectHelper.readConfigurationsFromSln(slnFile, wp7conf)
		assertNotNull(wp7conf.targets)
		assertNotNull(wp7conf.configurations)
	}


}
