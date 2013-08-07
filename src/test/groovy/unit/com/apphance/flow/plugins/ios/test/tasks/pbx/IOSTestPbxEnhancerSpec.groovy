package com.apphance.flow.plugins.ios.test.tasks.pbx

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.executor.command.CommandLogFilesGenerator
import com.apphance.flow.executor.linker.FileLinker
import groovy.json.JsonSlurper
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.STD
import static com.apphance.flow.plugins.ios.parsers.PbxJsonParser.PBX_SHELL_SCRIPT_BUILD_PHASE
import static com.google.common.io.Files.createTempDir
import static java.io.File.createTempFile

class IOSTestPbxEnhancerSpec extends Specification {

    @Shared
    def pbxJSON = new File('testProjects/ios/GradleXCode/GradleXCode.xcodeproj/project.pbxproj.json')

    @Ignore('This test hangs in Jenkins, INVESTIGATE!!')
    def 'shell script is added to pbx'() {
        given:
        def tmpDir = createTempDir()

        and:
        def conf = GroovyStub(IOSConfiguration) {
            getXcodeDir() >> new FileProperty(value: 'GradleXCode/GradleXCode.xcodeproj')
            getRootDir() >> tmpDir
        }

        and:
        def fileLinker = GroovyMock(FileLinker)
        def logFileGenerator = GroovyMock(CommandLogFilesGenerator) {
            commandLogFiles() >> [(STD): createTempFile('tmp', 'file-out'), (ERR): createTempFile('tmp', 'file-err')]
        }

        def commandExecutor = new CommandExecutor(fileLinker, logFileGenerator)

        and:
        def executor = GroovySpy(IOSExecutor) {
            pbxProjToJSON(_) >> pbxJSON.text.split('\n')
        }
        executor.conf = conf
        executor.executor = commandExecutor

        and:
        def variant = GroovyMock(IOSVariant) {
            getTarget() >> 'GradleXCode'
            getTmpDir() >> tmpDir
            getPbxFile() >> new File(tmpDir, 'GradleXCode/GradleXCode.xcodeproj/project.pbxproj')
        }
        and:
        def enhancer = new IOSTestPbxEnhancer()
        enhancer.executor = executor

        and:
        new File(tmpDir, "${conf.xcodeDir.value}").mkdirs()

        when:
        enhancer.addShellScriptToBuildPhase(variant, ['D382B73414703FE500E9CC9B'])

        then:
        def pbx = new File(tmpDir, 'GradleXCode/GradleXCode.xcodeproj/project.pbxproj')
        pbx.exists()
        pbx.size() > 0

        when:
        new XmlSlurper().parse(pbx)

        then:
        noExceptionThrown()

        when:
        def baos = new ByteArrayOutputStream()
        def p = "plutil -convert json $pbx -o -".split().execute()
        p.waitFor()
        p.consumeProcessOutputStream(baos)

        then:
        def json = new JsonSlurper().parseText(baos.toString()) as Map
        def scripts = json.objects.findAll { it.value.isa == PBX_SHELL_SCRIPT_BUILD_PHASE && it.key.size() > 24 }
        scripts.size() == 1
        def script = scripts.iterator().next()
        def scriptKey = script.key
        json.objects['D382B73414703FE500E9CC9B'].buildPhases.contains(scriptKey)
        script.value.isa == PBX_SHELL_SCRIPT_BUILD_PHASE
        script.value.buildActionMask.toString().matches('\\d+')
        script.value.files == []
        script.value.inputPaths == []
        script.value.outputPaths == []
        script.value.runOnlyForDeploymentPostprocessing == 0
        script.value.shellPath == '/bin/sh'
        script.value.showEnvVarsInLog == 1
        script.value.shellScript == getClass().getResource('run_ios_tests.sh').text

        cleanup:
        tmpDir.deleteDir()
    }

}
