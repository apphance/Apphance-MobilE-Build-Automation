package com.apphance.ameba.unit.ios;

import static org.junit.Assert.*
import org.junit.*

import com.apphance.ameba.ios.PbxProjectHelper;

class IOSPBXProjectHelperTest {

	@Test
	void parseProjectTest() {
		PbxProjectHelper helper = new PbxProjectHelper()
		Object o = helper.getParsedProject(new File("testProjects/ios/GradleXCode/"), "GradleXCode")
		assertNotNull(o)
		assertTrue(o.size() > 0)
	}

	@Test
	void addApphanceToProject() {
		PbxProjectHelper helper = new PbxProjectHelper()
		String s = helper.addApphanceToProject(new File("testProjects/ios/GradleXCode/"), "GradleXCode", "Debug", "44f53b811511860be73c78b2f3dc8c41d50e12f9")
		assertTrue(s.contains("Apphance-iOS.framework"))
	}
}
