package com.apphance.flow.plugins.ios.ocunit.tasks

import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.executor.IOSExecutor
import groovy.json.JsonSlurper

import javax.inject.Inject
import java.util.concurrent.atomic.AtomicInteger

import static com.apphance.flow.plugins.ios.parsers.PbxJsonParser.PBX_NATIVE_TARGET
import static com.apphance.flow.plugins.ios.parsers.PbxJsonParser.PBX_SHELL_SCRIPT_BUILD_PHASE
import static groovy.json.JsonOutput.toJson
import static java.io.File.createTempFile
import static java.lang.System.currentTimeMillis
import static java.security.MessageDigest.getInstance
import static org.gradle.api.logging.Logging.getLogger

class IOSTestPbxEnhancer {

    private logger = getLogger(getClass())

    @Inject IOSExecutor executor
    private AtomicInteger hash = new AtomicInteger()

    void addShellScriptToBuildPhase(IOSVariant variant, List<String> blueprintIds) {
        def json = jsonC.call(variant)
        def buildPhaseHash = addNewBuildPhase(json)
        addBuildPhaseToTargets(json, blueprintIds, buildPhaseHash)
        saveModifiedPbx(variant, json)
    }

    String addNewBuildPhase(Map json) {
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

    String shellScript() {
        getClass().getResource('run_ios_tests.sh').text
    }

    void addBuildPhaseToTargets(Map json, List<String> blueprintIds, String buildPhaseHash) {
        blueprintIds.each { String blueprintId ->
            def objects = json.objects
            def targetObject = objects.find { it.value.isa == PBX_NATIVE_TARGET && it.key == blueprintId }.value as Map
            targetObject.buildPhases << buildPhaseHash
        }
    }

    private void saveModifiedPbx(IOSVariant variant, Map json) {
        def pbx = variant.variantPbx
        logger.info("Saving PBX with Apphance added to file: $pbx.absolutePath")
        def tmpJson = createTempFile('pbx', 'json')
        tmpJson.text = toJson(json)
        pbx.text = executor.plistToXML(tmpJson).join('\n')
        tmpJson.delete()
    }

    @Lazy
    private Closure<Map> jsonC = { IOSVariant variant ->
        new JsonSlurper().parseText(executor.pbxProjToJSON(variant.variantPbx).join('\n')) as Map
    }.memoize()

    //TODO groovy.lang.Mixin
    private String hash() {
        md5(hash.incrementAndGet().toString()).toUpperCase()
    }

    private String md5(String s) {
        def digest = getInstance('MD5')
        digest.update(s.bytes);
        new BigInteger(1, digest.digest()).toString(16).padLeft(32, '0')
    }
}
