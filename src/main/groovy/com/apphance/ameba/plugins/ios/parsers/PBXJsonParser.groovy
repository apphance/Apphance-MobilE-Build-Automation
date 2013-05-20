package com.apphance.ameba.plugins.ios.parsers

import com.apphance.ameba.executor.IOSExecutor
import groovy.json.JsonSlurper

import javax.inject.Inject

class PbxJsonParser {

    static final INFOPLIST_FILE = 'INFOPLIST_FILE'

    @Inject
    IOSExecutor executor

    String plistForScheme(String configuration, String blueprintId) {
        def json = parsedPBX()
        def objects = json.objects

        def targetObject = objects.find { it.key == blueprintId }
        def buildConfigurationListKey = targetObject.value.buildConfigurationList
        def conf = findConfiguration(buildConfigurationListKey, configuration)

        conf.value.buildSettings[INFOPLIST_FILE]
    }

    String plistForTC(String target, String configuration) {
        def json = parsedPBX()
        def objects = json.objects

        def targetObject = objects.find { it.value.isa == 'PBXNativeTarget' && it.value.name == target }
        def buildConfigurationListKey = targetObject.value.buildConfigurationList
        def conf = findConfiguration(buildConfigurationListKey, configuration)

        conf.value.buildSettings[INFOPLIST_FILE]
    }

    private def findConfiguration(String buildConfigurationListKey, String configuration) {
        def json = parsedPBX()
        def objects = json.objects

        def buildConfigurationList = objects.find { it.key == buildConfigurationListKey }
        def buildConfigurations = buildConfigurationList.value.buildConfigurations
        def configurations = objects.findAll { it.key in buildConfigurations }
        def conf = configurations.find { it.value.isa == 'XCBuildConfiguration' && it.value.name == configuration }

        conf
    }

    String targetForBlueprintId(String blueprintId) {
        def json = parsedPBX()
        def objects = json.objects

        def targetObject = objects.find { it.value.isa == 'PBXNativeTarget' && it.key == blueprintId }

        targetObject.value.name
    }

    private Object parsedPBX() {
        new JsonSlurper().parseText(executor.pbxProjToJSON().join('\n'))
    }
}
