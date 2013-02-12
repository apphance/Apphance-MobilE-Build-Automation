package com.apphance.ameba.unit.ios

import com.apphance.ameba.ios.plugins.apphance.PbxProjectHelper
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

class IOSPBXProjectHelperTest {

    @Test
    void parseProjectTest() {
        PbxProjectHelper helper = new PbxProjectHelper()
        Object o = helper.getParsedProject(new File("testProjects/ios/GradleXCode/GradleXCode.xcodeproj/project.pbxproj"))
        helper.setRootObject(o)
        assertNotNull(helper.getObject("D382B70814703FE500E9CC9B"))
        assertEquals("D382B70B14703FE500E9CC9B", helper.getProperty(helper.getObject("D382B70814703FE500E9CC9B"), "buildConfigurationList").text())
        assertEquals("1", helper.getProperty(o.dict, "archiveVersion").text())
        assertEquals("D382B70814703FE500E9CC9B", helper.getProperty(o.dict, "rootObject").text())
        def object = helper.getObject(helper.getProperty(o.dict, "rootObject").text())
        assertNotNull(object)
        String s = helper.writePlistToString();
        assertNotNull(o)
    }
}
