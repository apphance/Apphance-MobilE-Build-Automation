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
        def s = PropertyManager.listProperties(project, IOSProjectProperty.class, false,
                IOSProjectProperty.DESCRIPTION)
        String stringVersion = ""
        s.each { stringVersion = stringVersion + it +'\n'}
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
''',stringVersion)
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
        def s = PropertyManager.listProperties(project, IOSProjectProperty.class, true,
                IOSProjectProperty.DESCRIPTION)
        String stringVersion = ""
        s.each { stringVersion = stringVersion + it +'\n'}
        println stringVersion
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
''',stringVersion)
    }

    @Test
    void testIOSKIFPropertyNoComments () {
        ProjectBuilder projectBuilder = ProjectBuilder.builder()
        Project project = projectBuilder.build()
        project['ios.kif.configuration'] = "Kif"
        def s = PropertyManager.listProperties(project, IOSKifProperty.class, false,
                IOSKifProperty.DESCRIPTION)
        String stringVersion = ""
        s.each { stringVersion = stringVersion + it +'\n'}
        assertEquals('''###########################################################
# iOS KIF properties
###########################################################
ios.kif.configuration=Kif
''',stringVersion)
    }

    @Test
    void testIOSFoneMonkeyPropertyComments () {
        ProjectBuilder projectBuilder = ProjectBuilder.builder()
        Project project = projectBuilder.build()
        project['ios.kif.configuration'] = "Kif"
        def s = PropertyManager.listProperties(project, IOSKifProperty.class, true,
                IOSKifProperty.DESCRIPTION)
        String stringVersion = ""
        s.each { stringVersion = stringVersion + it +'\n'}
        assertEquals('''###########################################################
# iOS KIF properties
###########################################################
# KIF build configuration [optional]
ios.kif.configuration=Kif
''',stringVersion)
    }
    @Test
    void testIOSFoneMonkeyPropertyNoComments () {
        ProjectBuilder projectBuilder = ProjectBuilder.builder()
        Project project = projectBuilder.build()
        project['ios.fonemonkey.configuration'] = "FoneMonkey"
        def s = PropertyManager.listProperties(project, IOSFoneMonkeyProperty.class, false,
                IOSFoneMonkeyProperty.DESCRIPTION)
        String stringVersion = ""
        s.each { stringVersion = stringVersion + it +'\n'}
        assertEquals('''###########################################################
# iOS FoneMonkey properties
###########################################################
ios.fonemonkey.configuration=FoneMonkey
''',stringVersion)
    }

    @Test
    void testIOSKIFPropertyComments () {
        ProjectBuilder projectBuilder = ProjectBuilder.builder()
        Project project = projectBuilder.build()
        project['ios.fonemonkey.configuration'] = "FoneMonkey"
        def s = PropertyManager.listProperties(project, IOSFoneMonkeyProperty.class, true,
                IOSFoneMonkeyProperty.DESCRIPTION)
        String stringVersion = ""
        s.each { stringVersion = stringVersion + it +'\n'}
        assertEquals('''###########################################################
# iOS FoneMonkey properties
###########################################################
# FoneMonkey build configuration [optional]
ios.fonemonkey.configuration=FoneMonkey
''',stringVersion)
    }
}
