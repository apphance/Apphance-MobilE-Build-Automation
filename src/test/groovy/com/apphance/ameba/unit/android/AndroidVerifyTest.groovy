package com.apphance.ameba.unit.android;

import static org.junit.Assert.*;

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test
import com.apphance.ameba.android.AndroidCommandParser

class AndroidVerifyTest {
    @Test
    public void testReadTargets() throws Exception {
        String text = this.class.getResource("target_output.txt").content.text
        List targets = AndroidCommandParser.extractTargets(text)
        println targets
        assertEquals('[Google Inc.:Google APIs:10, Google Inc.:Google APIs:11,\
 Google Inc.:Google APIs:12, Google Inc.:Google APIs:13, Google Inc.:Google APIs:14,\
 Google Inc.:Google APIs:3, Google Inc.:Google APIs:4, Google Inc.:Google APIs:6,\
 Google Inc.:Google APIs:7, Google Inc.:Google APIs:8, Google Inc.:Google APIs:9,\
 KYOCERA Corporation:DTS Add-On:10, KYOCERA Corporation:DTS Add-On:8,\
 KYOCERA Corporation:DTS Add-On:8, LGE:Real3D Add-On:10, LGE:Real3D Add-On:8,\
 LGE:Real3D Add-On:8, Samsung Electronics Co., Ltd.:GALAXY Tab Addon:8,\
 Samsung Electronics Co., Ltd.:GALAXY Tab Addon:8, Samsung Electronics Co.,\
 Ltd.:GALAXY Tab Addon:8, Sony Ericsson Mobile Communications AB:EDK 1.2:10,\
 Sony Ericsson Mobile Communications AB:EDK:9,\
 android-10, android-11, android-12, android-13, android-14, android-2, android-3,\
 android-4, android-5, android-6, android-7, android-8, android-9]', targets.toString())
    }

    @Test
    public void testReadTargetsFromAndroidExcution() {
		ProjectBuilder builder = new ProjectBuilder()
		builder = builder.withProjectDir(new File('testProjects/android/'))
		Project project = builder.build()
        List targets = AndroidCommandParser.getTargets(project)
        println targets
        assertTrue(targets.size()>0)
    }
}
