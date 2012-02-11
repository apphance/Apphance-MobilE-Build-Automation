package com.apphance.ameba.unit.ios;

import static org.junit.Assert.*;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;


import com.apphance.ameba.PropertyManager;
import com.apphance.ameba.ios.IOSProjectProperty;
import com.apphance.ameba.ios.plugins.fonemonkey.IOSFoneMonkeyProperty;
import com.apphance.ameba.ios.plugins.kif.IOSKifProperty;

class IOSPropertyTest {
    @Test
    void testIOSPropertyNoComments () {
        ProjectBuilder projectBuilder = ProjectBuilder.builder()
        Project project = projectBuilder.build()
        project['ios.plist.file'] = "test"
        project['ios.excluded.builds'] = ".*"
        project['ios.families'] = "iPhone,iPad"
        project['ios.distribution.resources.dir'] = "release/distribution_resources"
        project['ios.mainTarget'] = "MainTarget"
        project['ios.mainConfiguration'] = "MainConfiguration"
        project['ios.sdk'] = "iphoneos"
        project['ios.simulator.sdk'] = "iphonesimulator"
        project['ios.fonemonkey.configuration'] = "FoneMonkey"
        project['ios.kif.configuration'] = "Kif"
        String s = PropertyManager.listPropertiesAsString(project, IOSProjectProperty.class, false)
        assertEquals('''###########################################################
# iOS properties
###########################################################
ios.plist.file=test
ios.excluded.builds=.*
ios.families=iPhone,iPad
ios.distribution.resources.dir=release/distribution_resources
ios.mainTarget=MainTarget
ios.mainConfiguration=MainConfiguration
ios.sdk=iphoneos
ios.simulator.sdk=iphonesimulator
''',s)
    }
    @Test
    void testIOSPropertyWithComments () {
        ProjectBuilder projectBuilder = ProjectBuilder.builder()
        Project project = projectBuilder.build()
        project['ios.plist.file'] = "test"
        project['ios.excluded.builds'] = ".*"
        project['ios.families'] = "iPhone,iPad"
        project['ios.distribution.resources.dir'] = "release/distribution_resources"
        project['ios.mainTarget'] = "MainTarget"
        project['ios.mainConfiguration'] = "MainConfiguration"
        project['ios.sdk'] = "iphoneos"
        project['ios.simulator.sdk'] = "iphonesimulator"
        project['ios.fonemonkey.configuration'] = "FoneMonkey"
        project['ios.kif.configuration'] = "Kif"
        String s = PropertyManager.listPropertiesAsString(project, IOSProjectProperty.class, true)
        assertEquals('''###########################################################
# iOS properties
###########################################################
# Path to plist file [required]
ios.plist.file=test
# List of excluded builds [required]
ios.excluded.builds=.*
# List of iOS families [required]
ios.families=iPhone,iPad
# Path to distribution resources directory [required]
ios.distribution.resources.dir=release/distribution_resources
# Main target for release build [optional]
ios.mainTarget=MainTarget
# Main configuration for release build [optional]
ios.mainConfiguration=MainConfiguration
# List of iOS SDKs [optional]
ios.sdk=iphoneos
# List of iOS simulator SDKs [optional]
ios.simulator.sdk=iphonesimulator
''',s)
    }

    @Test
    void testIOSKIFPropertyNoComments () {
        ProjectBuilder projectBuilder = ProjectBuilder.builder()
        Project project = projectBuilder.build()
        project['ios.kif.configuration'] = "Kif"
        String s = PropertyManager.listPropertiesAsString(project, IOSKifProperty.class, false)
        assertEquals('''###########################################################
# iOS KIF properties
###########################################################
ios.kif.configuration=Kif
''',s)
    }

    @Test
    void testIOSFoneMonkeyPropertyComments () {
        ProjectBuilder projectBuilder = ProjectBuilder.builder()
        Project project = projectBuilder.build()
        project['ios.kif.configuration'] = "Kif"
        String s = PropertyManager.listPropertiesAsString(project, IOSKifProperty.class, true)
        assertEquals('''###########################################################
# iOS KIF properties
###########################################################
# KIF build configuration [optional]
ios.kif.configuration=Kif
''',s)
    }
    @Test
    void testIOSFoneMonkeyPropertyNoComments () {
        ProjectBuilder projectBuilder = ProjectBuilder.builder()
        Project project = projectBuilder.build()
        project['ios.fonemonkey.configuration'] = "FoneMonkey"
        String s = PropertyManager.listPropertiesAsString(project, IOSFoneMonkeyProperty.class, false)
        assertEquals('''###########################################################
# iOS FoneMonkey properties
###########################################################
ios.fonemonkey.configuration=FoneMonkey
''',s)
    }

    @Test
    void testIOSKIFPropertyComments () {
        ProjectBuilder projectBuilder = ProjectBuilder.builder()
        Project project = projectBuilder.build()
        project['ios.fonemonkey.configuration'] = "FoneMonkey"
        String s = PropertyManager.listPropertiesAsString(project, IOSFoneMonkeyProperty.class, true)
        assertEquals('''###########################################################
# iOS FoneMonkey properties
###########################################################
# FoneMonkey build configuration [optional]
ios.fonemonkey.configuration=FoneMonkey
''',s)
    }
}
