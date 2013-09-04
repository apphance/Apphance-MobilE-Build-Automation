package com.apphance.flow.plugins.ios.parsers

import com.apphance.flow.executor.IOSExecutor
import groovy.json.JsonSlurper
import org.gradle.api.GradleException
import org.gradle.api.logging.Logging

import javax.inject.Inject

class PbxJsonParser {

    public static final String PBX_NATIVE_TARGET = 'PBXNativeTarget'
    public static final String PBX_FILE_REFERENCE = 'PBXFileReference'
    public static final String PBX_BUILD_FILE = 'PBXBuildFile'
    public static final String PBX_GROUP = 'PBXGroup'
    public static final String PBX_FRAMEWORKS_BUILD_PHASE = 'PBXFrameworksBuildPhase'
    public static final String PBX_SOURCES_BUILD_PHASE = 'PBXSourcesBuildPhase'
    public static final String PBX_SHELL_SCRIPT_BUILD_PHASE = 'PBXShellScriptBuildPhase'
    public static final String INFOPLIST_FILE = 'INFOPLIST_FILE'
    public static final String XCBUILD_CONFIGURATION = 'XCBuildConfiguration'

    private logger = Logging.getLogger(getClass())

    @Inject IOSExecutor executor

    String plistForScheme(File pbx, String configuration, String blueprintId) {
        logger.info("Looking for plist in file: $pbx.absolutePath, configuration: $configuration, blueprintId: $blueprintId")

        def json = parsedPBX(pbx)
        def objects = json.objects

        def targetObject = objects.find { it.key == blueprintId }.value as Map
        def buildConfigurationListKey = targetObject.buildConfigurationList
        def conf = findConfiguration(objects, buildConfigurationListKey, configuration)

        conf.buildSettings[INFOPLIST_FILE]
    }

    private Map findConfiguration(Map objects, String buildConfigurationListKey, String configuration) {

        def buildConfigurationList = objects.find { it.key == buildConfigurationListKey }.value as Map
        def buildConfigurations = buildConfigurationList.buildConfigurations
        def configurations = objects.findAll { it.key in buildConfigurations }
        def conf = configurations.find { it.value.isa == XCBUILD_CONFIGURATION && it.value.name == configuration }

        if (!conf)
            throw new GradleException("Impossible to find configuration $configuration in configuration list: $buildConfigurationListKey")

        conf.value as Map
    }

    String targetForBlueprintId(File pbx, String blueprintId) {
        logger.info("Looking for blueprintId: $blueprintId in file $pbx.absolutePath")

        def json = parsedPBX(pbx)
        def objects = json.objects

        def targetObject = objects.find { it.value.isa == PBX_NATIVE_TARGET && it.key == blueprintId }.value as Map

        targetObject.name
    }

    boolean isFrameworkDeclared(File pbx, def frameworkNamePattern) {
        def json = parsedPBX(pbx)
        json.objects.find { it.value.isa == PBX_FILE_REFERENCE && it.value.name =~ frameworkNamePattern }
    }

    private Map parsedPBX(File pbx) {
        new JsonSlurper().parseText(executor.pbxProjToJSON(pbx).join('\n')) as Map
    }
}
