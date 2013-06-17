package com.apphance.flow.plugins.ios.parsers

import com.apphance.flow.executor.IOSExecutor
import groovy.json.JsonSlurper

import javax.inject.Inject

import static org.apache.commons.lang.StringUtils.isNotBlank

class PbxJsonParser {

    public static final String PBX_NATIVE_TARGET = 'PBXNativeTarget'
    public static final String PBX_FILE_REFERENCE = 'PBXFileReference'
    public static final String PBX_BUILD_FILE = 'PBXBuildFile'
    public static final String PBX_GROUP = 'PBXGroup'
    public static final String PBX_FRAMEWORKS_BUILD_PHASE = 'PBXFrameworksBuildPhase'
    public static final String PBX_SOURCES_BUILD_PHASE = 'PBXSourcesBuildPhase'
    public static final String INFOPLIST_FILE = 'INFOPLIST_FILE'
    public static final String XCBUILD_CONFIGURATION = 'XCBuildConfiguration'

    @Inject IOSExecutor executor

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

        def targetObject = objects.find { it.value.isa == PBX_NATIVE_TARGET && it.value.name == target }
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
        def conf = configurations.find { it.value.isa == XCBUILD_CONFIGURATION && it.value.name == configuration }

        conf
    }

    String targetForBlueprintId(String blueprintId) {
        def json = parsedPBX()
        def objects = json.objects

        def targetObject = objects.find { it.value.isa == PBX_NATIVE_TARGET && it.key == blueprintId }

        targetObject.value.name
    }

    boolean isFrameworkDeclared(def frameworkNamePattern) {
        def json = parsedPBX()
        json.objects.find { it.value.isa == PBX_FILE_REFERENCE && it.value.name =~ frameworkNamePattern }
    }

    private Object parsedPBX() {
        new JsonSlurper().parseText(executor.pbxProjToJSON.join('\n'))
    }

    static boolean isPlaceholder(String value) {
        isNotBlank(value) && value.matches('\\$\\(([A-Z]+_)*([A-Z])+\\)')
    }
}
