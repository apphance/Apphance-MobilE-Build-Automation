package com.apphance.ameba.unit.android;

import static org.junit.Assert.*;

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test



import com.apphance.ameba.PropertyCategory;
import com.apphance.ameba.android.plugins.buildplugin.AndroidProjectProperty;

class AndroidPropertyTest {
    @Test
    void testAndroidPropertyNoComments () {
        use (PropertyCategory) {
            ProjectBuilder projectBuilder = ProjectBuilder.builder()
            Project project = projectBuilder.build()
            project[AndroidProjectProperty.MAIN_VARIANT.propertyName] = "mainVariant"
            project[AndroidProjectProperty.EXCLUDED_BUILDS.propertyName] = ".*"
            project[AndroidProjectProperty.MIN_SDK_TARGET.propertyName] = "android-8"
            String s = project.listPropertiesAsString(AndroidProjectProperty.class, false)
            println s
            assertEquals('''###########################################################
# Android properties
###########################################################
android.mainVariant=mainVariant
android.excluded.builds=.*
android.minSdk.target=android-8
''',s)
        }
    }
    @Test
    void testAndroidPropertyWithComments () {
        use (PropertyCategory) {
            ProjectBuilder projectBuilder = ProjectBuilder.builder()
            Project project = projectBuilder.build()
            project[AndroidProjectProperty.MAIN_VARIANT.propertyName] = "mainVariant"
            project[AndroidProjectProperty.EXCLUDED_BUILDS.propertyName] = ".*"
            project[AndroidProjectProperty.MIN_SDK_TARGET.propertyName] = "android-8"
            String s = project.listPropertiesAsString(AndroidProjectProperty.class, true)
            println s
            assertEquals('''###########################################################
# Android properties
###########################################################
# Main variant used when releasing the aplication [optional] default<>
android.mainVariant=mainVariant
# Regular expressions separated with comas - if variant name matches any of these, it is excluded from configuration [optional] default: <>
android.excluded.builds=.*
# Minimum target against which source code analysis is done - the project will fail Java compilation in case classes from higher target are used [optional] default: <>
android.minSdk.target=android-8
''',s)
        }
    }
}
