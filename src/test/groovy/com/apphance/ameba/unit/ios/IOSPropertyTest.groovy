package com.apphance.ameba.unit.ios;

import static org.junit.Assert.*;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;


import com.apphance.ameba.PropertyCategory;
import com.apphance.ameba.ios.IOSProjectProperty;
import com.apphance.ameba.ios.plugins.fonemonkey.IOSFoneMonkeyProperty;
import com.apphance.ameba.ios.plugins.kif.IOSKifProperty;

class IOSPropertyTest {
    @Test
    void testIOSPropertyNoComments () {
        use (PropertyCategory) {
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
            String s = project.listPropertiesAsString(IOSProjectProperty.class, false)
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
    }
    @Test
    void testIOSPropertyWithComments () {
        use (PropertyCategory) {
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
            String s = project.listPropertiesAsString(IOSProjectProperty.class, true)
            assertEquals('''###########################################################
# iOS properties
###########################################################
# Path to plist file of the project
ios.plist.file=test
# List of excluded builds. These are coma-separated regular expressions (matched against target-configuration) [optional] default: <>
ios.excluded.builds=.*
# List of iOS families used (iPhone/iPad) [optional] default: <iPhone,iPad>
ios.families=iPhone,iPad
# Path to distribution resources directory. In this directory mobile provision file should be placed.
ios.distribution.resources.dir=release/distribution_resources
# Main target for releaseable build
ios.mainTarget=MainTarget
# Main configuration for releaseable build
ios.mainConfiguration=MainConfiguration
# SDK used to build iOS targets (-sdk option of xcodebuild) [optional] default: <iphoneos>
ios.sdk=iphoneos
# SDK used to build simulator targets (-sdk option of xcodebuild) [optional] default: <iphonesimulator>
ios.simulator.sdk=iphonesimulator
''',s)
        }
    }

    @Test
    void testIOSKIFPropertyNoComments () {
        use (PropertyCategory) {
            ProjectBuilder projectBuilder = ProjectBuilder.builder()
            Project project = projectBuilder.build()
            project['ios.kif.configuration'] = "Kif"
            String s = project.listPropertiesAsString(IOSKifProperty.class, false)
            assertEquals('''###########################################################
# iOS KIF properties
###########################################################
ios.kif.configuration=Kif
''',s)
        }
    }

    @Test
    void testIOSFoneMonkeyPropertyComments () {
        use (PropertyCategory) {
            ProjectBuilder projectBuilder = ProjectBuilder.builder()
            Project project = projectBuilder.build()
            project['ios.kif.configuration'] = "Kif"
            String s = project.listPropertiesAsString(IOSKifProperty.class, true)
            assertEquals('''###########################################################
# iOS KIF properties
###########################################################
# KIF build configuration [optional] default: <Debug>
ios.kif.configuration=Kif
''',s)
        }
    }
    @Test
    void testIOSFoneMonkeyPropertyNoComments () {
        use (PropertyCategory) {
            ProjectBuilder projectBuilder = ProjectBuilder.builder()
            Project project = projectBuilder.build()
            project['ios.fonemonkey.configuration'] = "FoneMonkey"
            String s = project.listPropertiesAsString(IOSFoneMonkeyProperty.class, false)
            assertEquals('''###########################################################
# iOS FoneMonkey properties
###########################################################
ios.fonemonkey.configuration=FoneMonkey
''',s)
        }
    }

    @Test
    void testIOSKIFPropertyComments () {
        use (PropertyCategory) {
            ProjectBuilder projectBuilder = ProjectBuilder.builder()
            Project project = projectBuilder.build()
            project['ios.fonemonkey.configuration'] = "FoneMonkey"
            String s = project.listPropertiesAsString(IOSFoneMonkeyProperty.class, true)
            assertEquals('''###########################################################
# iOS FoneMonkey properties
###########################################################
# FoneMonkey build configuration [optional] default: <Debug>
ios.fonemonkey.configuration=FoneMonkey
''',s)
        }
    }
}
