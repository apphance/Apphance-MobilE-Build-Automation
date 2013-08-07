package com.apphance.flow.plugins.ios.test.tasks.pbx

import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.parsers.PbxHashGenerator
import groovy.json.JsonSlurper

import javax.inject.Inject

import static com.apphance.flow.plugins.ios.parsers.PbxJsonParser.PBX_NATIVE_TARGET
import static com.apphance.flow.plugins.ios.parsers.PbxJsonParser.PBX_SHELL_SCRIPT_BUILD_PHASE
import static groovy.json.JsonOutput.toJson
import static java.io.File.createTempFile
import static java.lang.System.currentTimeMillis
import static org.gradle.api.logging.Logging.getLogger

@Mixin(PbxHashGenerator)
class IOSTestPbxEnhancer {

    private logger = getLogger(getClass())

    @Inject IOSExecutor executor

    void addShellScriptToBuildPhase(IOSVariant variant, List<String> blueprintIds) {
        def json = jsonC.call(variant)
        def buildPhaseHash = addNewBuildPhase(json)
        addBuildPhaseToTargets(json, blueprintIds, buildPhaseHash)
        saveModifiedPbx(variant.pbxFile, json)
    }

    private String addNewBuildPhase(Map json) {
        String buildPhaseHash = hash()
        json.objects[buildPhaseHash] = [
                isa: PBX_SHELL_SCRIPT_BUILD_PHASE,
                buildActionMask: currentTimeMillis(),
                files: [],
                inputPaths: [],
                outputPaths: [],
                runOnlyForDeploymentPostprocessing: 0,
                shellPath: '/bin/sh',
                shellScript: shellScript(),
                showEnvVarsInLog: 0
        ]
        buildPhaseHash
    }

    private String shellScript() {
        getClass().getResource('run_ios_tests.sh').text
    }

    private void addBuildPhaseToTargets(Map json, List<String> blueprintIds, String buildPhaseHash) {
        blueprintIds.each { String blueprintId ->
            def objects = json.objects
            def targetObject = objects.find { it.value.isa == PBX_NATIVE_TARGET && it.key == blueprintId }.value as Map
            targetObject.buildPhases << buildPhaseHash
        }
    }

    private void saveModifiedPbx(File pbx, Map json) {
        logger.info("Saving PBX with test shell script added to file: $pbx.absolutePath")
        def tmpJson = createTempFile('pbx', 'json')
        tmpJson.text = toJson(json)
        pbx.text = executor.plistToXML(tmpJson).join('\n')
        tmpJson.delete()
    }

    @Lazy
    private Closure<Map> jsonC = { IOSVariant variant ->
        new JsonSlurper().parseText(executor.pbxProjToJSON(variant.pbxFile).join('\n')) as Map
    }.memoize()
}
