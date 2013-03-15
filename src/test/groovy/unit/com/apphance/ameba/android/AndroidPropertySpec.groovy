package com.apphance.ameba.android

import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.android.plugins.buildplugin.AndroidProjectProperty
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import static com.apphance.ameba.android.plugins.buildplugin.AndroidProjectProperty.*
import static com.apphance.ameba.android.plugins.buildplugin.AndroidProjectProperty.MAIN_VARIANT

class AndroidPropertySpec extends Specification {

    Project project = ProjectBuilder.builder().build()

    def setup(){
        project[MAIN_VARIANT.propertyName] = "mainVariant"
        project[EXCLUDED_BUILDS.propertyName] = ".*"
        project[MIN_SDK_TARGET.propertyName] = "android-8"
    }

    def testAndroidPropertyNoComments() {
        when:
        def properties = PropertyCategory.listPropertiesAsString(project, AndroidProjectProperty, false)

        then:
        '''|###########################################################
           |# Android properties
           |###########################################################
           |android.mainVariant=mainVariant
           |android.excluded.builds=.*
           |android.minSdk.target=android-8
           |'''.stripMargin() == properties
    }

    def testAndroidPropertyWithComments() {
        when:
        def properties = PropertyCategory.listPropertiesAsString(project, AndroidProjectProperty, true)

        then:
        '''|###########################################################
           |# Android properties
           |###########################################################
           |# Main variant used when releasing the aplication [optional] default: <>
           |android.mainVariant=mainVariant
           |# Regular expressions separated with comas - if variant name matches any of these, it is excluded from configuration [optional] default: <>
           |android.excluded.builds=.*
           |# Minimum target against which source code analysis is done - the project will fail Java compilation in case classes from higher target are used [optional] default: <>
           |android.minSdk.target=android-8
           |'''.stripMargin() == properties
    }
}
