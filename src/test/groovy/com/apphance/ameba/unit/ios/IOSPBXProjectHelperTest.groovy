package com.apphance.ameba.unit.ios;

import static org.junit.Assert.*
import org.junit.*

import com.apphance.ameba.ProjectHelper;
import com.apphance.ameba.ios.PbxProjectHelper;

class IOSPBXProjectHelperTest {

	@Test
	void parseProjectTest() {
		PbxProjectHelper helper = new PbxProjectHelper()
		Object o = helper.getParsedProject(new File("testProjects/ios/GradleXCode/"), "GradleXCode")
		assertNotNull(o)
		assertTrue(o.size() > 0)
	}
}
