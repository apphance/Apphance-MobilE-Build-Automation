package com.apphance.ameba.android

import com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin
import com.apphance.ameba.plugins.AmebaPlugin
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Ignore
import spock.lang.Specification

class AndroidVerifyTest extends Specification {

    @Ignore('ignored till refactorś')
    def "test read targets"() {
        given:
        String target_output = this.class.getResource("target_output.txt").content.text
        def expectedTargets = ['Google Inc.:Google APIs:10', 'Google Inc.:Google APIs:11', 'Google Inc.:Google APIs:12', 'Google Inc.:Google APIs:13',
                'Google Inc.:Google APIs:14', 'Google Inc.:Google APIs:3', 'Google Inc.:Google APIs:4', 'Google Inc.:Google APIs:6', 'Google Inc.:Google APIs:7',
                'Google Inc.:Google APIs:8', 'Google Inc.:Google APIs:9', 'KYOCERA Corporation:DTS Add-On:10', 'KYOCERA Corporation:DTS Add-On:8',
                'KYOCERA Corporation:DTS Add-On:8', 'LGE:Real3D Add-On:10', 'LGE:Real3D Add-On:8', 'LGE:Real3D Add-On:8',
                'Samsung Electronics Co., Ltd.:GALAXY Tab Addon:8', 'Samsung Electronics Co., Ltd.:GALAXY Tab Addon:8',
                'Samsung Electronics Co., Ltd.:GALAXY Tab Addon:8', 'Sony Ericsson Mobile Communications AB:EDK 1.2:10',
                'Sony Ericsson Mobile Communications AB:EDK:9', 'android-10', 'android-11', 'android-12', 'android-13', 'android-14', 'android-2',
                'android-3', 'android-4', 'android-5', 'android-6', 'android-7', 'android-8', 'android-9']

        when:
        List targets = AndroidPlugin.extractAvailableTargets(target_output)

        then:
        expectedTargets.size() == targets.size()
        expectedTargets as Set == targets as Set
    }

    def "test read targets from android execution"() {
        given:
        Project project = ProjectBuilder.builder().withProjectDir(new File("testProjects/android/android-basic")).build()

        when:
        project.plugins.apply AmebaPlugin
        def plugin = project.plugins.getPlugin AndroidPlugin
        def targets = plugin.androidConf.availableTargets

        then:
        'Google Inc.:Google APIs:10' in targets
        'Google Inc.:Google APIs:14' in targets
        'android-7' in targets
    }
}
