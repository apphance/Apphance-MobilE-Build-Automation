package com.apphance.ameba.unit.ios;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.AfterClass
import org.junit.Test

import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.plugins.buildplugin.IOSProjectProperty
import com.apphance.ameba.ios.plugins.fonemonkey.FoneMonkeyProperty
import com.apphance.ameba.ios.plugins.framework.IOSFrameworkProperty
import com.apphance.ameba.ios.plugins.kif.KifProperty

class IOSPropertyTest {
    @Test
    void testIOSPropertyNoComments () {
        use (PropertyCategory) {
            ProjectBuilder projectBuilder = ProjectBuilder.builder()
            Project project = projectBuilder.build()
            project[IOSProjectProperty.PROJECT_DIRECTORY.propertyName] = "Project.xcodeproj"
            project[IOSProjectProperty.PLIST_FILE.propertyName] = "test"
            project[IOSProjectProperty.EXCLUDED_BUILDS.propertyName] = ".*"
            project[IOSProjectProperty.IOS_FAMILIES.propertyName] = "iPhone,iPad"
            project[IOSProjectProperty.DISTRIBUTION_DIR.propertyName] = "release/distribution_resources"
            project[IOSProjectProperty.MAIN_TARGET.propertyName] = "MainTarget"
            project[IOSProjectProperty.MAIN_CONFIGURATION.propertyName] = "MainConfiguration"
            project[IOSProjectProperty.IOS_SDK.propertyName] = "iphoneos"
            project[IOSProjectProperty.IOS_SIMULATOR_SDK.propertyName] = "iphonesimulator"
            project[FoneMonkeyProperty.FONE_MONKEY_CONFIGURATION.propertyName] = "FoneMonkey"
            project[KifProperty.KIF_CONFIGURATION.propertyName] = "Kif"
            String s = project.listPropertiesAsString(IOSProjectProperty.class, false)
            assertEquals('''###########################################################
# iOS properties
###########################################################
ios.project.directory=Project.xcodeproj
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
            project[IOSProjectProperty.PROJECT_DIRECTORY.propertyName] = "Project.xcodeproj"
            project[IOSProjectProperty.PLIST_FILE.propertyName] = "test"
            project[IOSProjectProperty.EXCLUDED_BUILDS.propertyName] = ".*"
            project[IOSProjectProperty.IOS_FAMILIES.propertyName] = "iPhone,iPad"
            project[IOSProjectProperty.DISTRIBUTION_DIR.propertyName] = "release/distribution_resources"
            project[IOSProjectProperty.MAIN_TARGET.propertyName] = "MainTarget"
            project[IOSProjectProperty.MAIN_CONFIGURATION.propertyName] = "MainConfiguration"
            project[IOSProjectProperty.IOS_SDK.propertyName] = "iphoneos"
            project[IOSProjectProperty.IOS_SIMULATOR_SDK.propertyName] = "iphonesimulator"
            project[FoneMonkeyProperty.FONE_MONKEY_CONFIGURATION.propertyName] = "FoneMonkey"
            project[KifProperty.KIF_CONFIGURATION.propertyName] = "Kif"
            String s = project.listPropertiesAsString(IOSProjectProperty.class, true)
            assertEquals('''###########################################################
# iOS properties
###########################################################
# Path to appropriate .xcodeproj directory
ios.project.directory=Project.xcodeproj
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
            project[KifProperty.KIF_CONFIGURATION.propertyName] = "Kif"
            String s = project.listPropertiesAsString(KifProperty.class, false)
            assertEquals('''###########################################################
# iOS KIF properties
###########################################################
ios.kif.configuration=Kif
''',s)
        }
    }

    @Test
    void testIOSKIFPropertyComments () {
        use (PropertyCategory) {
            ProjectBuilder projectBuilder = ProjectBuilder.builder()
            Project project = projectBuilder.build()
            project[KifProperty.KIF_CONFIGURATION.propertyName] = "Kif"
            String s = project.listPropertiesAsString(KifProperty.class, true)
            assertEquals('''###########################################################
# iOS KIF properties
###########################################################
# KIF build configuration [optional] default: <Debug>
ios.kif.configuration=Kif
''',s)
        }
    }

    @Test
    void testFoneMonkeyPropertyNoComments () {
        use (PropertyCategory) {
            ProjectBuilder projectBuilder = ProjectBuilder.builder()
            Project project = projectBuilder.build()
            project[FoneMonkeyProperty.FONE_MONKEY_CONFIGURATION.propertyName] = "FoneMonkey"
            String s = project.listPropertiesAsString(FoneMonkeyProperty.class, false)
            assertEquals('''###########################################################
# iOS FoneMonkey properties
###########################################################
ios.fonemonkey.configuration=FoneMonkey
''',s)
        }
    }


    @Test
    void testFoneMonkeyPropertyComments () {
        use (PropertyCategory) {
            ProjectBuilder projectBuilder = ProjectBuilder.builder()
            Project project = projectBuilder.build()
            project[FoneMonkeyProperty.FONE_MONKEY_CONFIGURATION.propertyName] = "FoneMonkey"
            String s = project.listPropertiesAsString(FoneMonkeyProperty.class, true)
            assertEquals('''###########################################################
# iOS FoneMonkey properties
###########################################################
# FoneMonkey build configuration [optional] default: <Debug>
ios.fonemonkey.configuration=FoneMonkey
''',s)
        }
    }

    @Test
    void testIOSFrameworkPropertyNoComments () {
        use (PropertyCategory) {
            ProjectBuilder projectBuilder = ProjectBuilder.builder()
            Project project = projectBuilder.build()
            project[IOSFrameworkProperty.FRAMEWORK_TARGET.propertyName] = "Target"
            project[IOSFrameworkProperty.FRAMEWORK_CONFIGURATION.propertyName] = "Debug"
            project[IOSFrameworkProperty.FRAMEWORK_VERSION.propertyName] = "A"
            project[IOSFrameworkProperty.FRAMEWORK_HEADERS.propertyName] = "Headers/test.h, Headers/test2.h"
            project[IOSFrameworkProperty.FRAMEWORK_RESOURCES.propertyName] = "Resources/x.png"
            String s = project.listPropertiesAsString(IOSFrameworkProperty.class, false)
            println s
            assertEquals('''###########################################################
# iOS Framework properties
###########################################################
ios.framework.target=Target
ios.framework.configuration=Debug
ios.framework.version=A
ios.framework.headers=Headers/test.h, Headers/test2.h
ios.framework.resources=Resources/x.png
''',s)
        }
    }

    @Test
    void testIOSFrameworkPropertyComments () {
        use (PropertyCategory) {
            ProjectBuilder projectBuilder = ProjectBuilder.builder()
            Project project = projectBuilder.build()
            project[IOSFrameworkProperty.FRAMEWORK_TARGET.propertyName] = "Target"
            project[IOSFrameworkProperty.FRAMEWORK_CONFIGURATION.propertyName] = "Debug"
            project[IOSFrameworkProperty.FRAMEWORK_VERSION.propertyName] = "A"
            project[IOSFrameworkProperty.FRAMEWORK_HEADERS.propertyName] = "Headers/test.h, Headers/test2.h"
            project[IOSFrameworkProperty.FRAMEWORK_RESOURCES.propertyName] = "Resources/x.png"
            String s = project.listPropertiesAsString(IOSFrameworkProperty.class, true)
            println s
            assertEquals('''###########################################################
# iOS Framework properties
###########################################################
# Target to build framework project with
ios.framework.target=Target
# Configuration to build framework project with [optional] default: <Debug>
ios.framework.configuration=Debug
# Version of framework (usually single alphabet letter A) [optional] default: <A>
ios.framework.version=A
# List of headers (coma separated) that should be copied to the framework
ios.framework.headers=Headers/test.h, Headers/test2.h
# List of resources (coma separated) that should be copied to the framework
ios.framework.resources=Resources/x.png
''',s)
        }
    }
}
