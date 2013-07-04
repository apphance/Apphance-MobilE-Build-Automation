package com.apphance.flow.plugins.ios.apphance.pbx

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.configuration.properties.ApphanceModeProperty
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.executor.command.CommandLogFilesGenerator
import com.apphance.flow.executor.linker.FileLinker
import com.apphance.flow.plugins.ios.parsers.PbxJsonParser
import groovy.json.JsonSlurper
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import static com.apphance.flow.configuration.apphance.ApphanceMode.QA
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.STD
import static com.apphance.flow.plugins.ios.parsers.PbxJsonParser.*
import static com.google.common.io.Files.createTempDir
import static java.io.File.createTempFile

class IOSApphancePbxEnhancerSpec extends Specification {

    @Shared
    def pbxJSON = new File('testProjects/ios/GradleXCode/GradleXCode.xcodeproj/project.pbxproj.json')

    def 'basic pbx json objects found'() {
        given:
        def enhancer = new IOSApphancePbxEnhancer(GroovyMock(IOSVariant) {
            getTarget() >> 'GradleXCode'
            getConfiguration() >> 'BasicConfiguration'
        })
        enhancer.iosExecutor = GroovyMock(IOSExecutor) {
            pbxProjToJSON(_) >> pbxJSON.text.split('\n')
        }

        expect:
        enhancer.rootObject.isa == 'PBXProject'
        enhancer.target.name == 'GradleXCode'
        enhancer.configuration.name == 'BasicConfiguration'
        enhancer.frameworksBuildPhase.files.containsAll(
                'D382B71614703FE500E9CC9B', 'D382B71814703FE500E9CC9B', 'D382B71A14703FE500E9CC9B'
        )
        enhancer.mainGroup.children.containsAll(
                'D382B71B14703FE500E9CC9B', 'D382B73C14703FE500E9CC9B', 'D382B71414703FE500E9CC9B', 'D382B71214703FE500E9CC9B'
        )
        enhancer.GCCPrefixFilePath == 'GradleXCode/GradleXCode-Prefix.pch'
        enhancer.filesToReplaceLogs.size() > 0
        enhancer.mainGroupFrameworks.name == 'Frameworks'
    }

    @Ignore('this test hangs in Jenkins ???')
    def 'apphance is added to pbx'() {
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
            getConfiguration() >> 'BasicConfiguration'
            getApphanceMode() >> new ApphanceModeProperty(value: QA)
            getTmpDir() >> tmpDir
            getVariantPbx() >> GroovyMock(File)
        }
        and:
        def enhancer = new IOSApphancePbxEnhancer(variant)
        enhancer.conf = conf
        enhancer.iosExecutor = executor
        enhancer.pbxJsonParser = new PbxJsonParser(executor: executor)

        and:
        new File(tmpDir, "${conf.xcodeDir.value}").mkdirs()

        when:
        enhancer.addApphanceToPbx()

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
        def rootObject = json.objects[json.rootObject] as Map
        def target = json.objects.find {
            it.key in rootObject.targets && it.value.isa == PBX_NATIVE_TARGET && it.value.name == variant.target
        }.value as Map
        def confHashes = json.objects[target.buildConfigurationList].buildConfigurations
        def configuration = json.objects.find {
            it.key in confHashes && it.value.isa == XCBUILD_CONFIGURATION && it.value.name == variant.configuration
        }.value as Map

        and:
        configuration.buildSettings.OTHER_LDFLAGS == ['-ObjC', '-all_load']
        configuration.buildSettings.FRAMEWORK_SEARCH_PATHS == ['$(inherited)', '"$(SRCROOT)"']
        configuration.buildSettings.LIBRARY_SEARCH_PATHS == ['$(inherited)', "\"\$(SRCROOT)/Apphance-Pre-Production.framework\""]

        and:
        def frameworks = json.objects.findAll { it.value.isa == PBX_FILE_REFERENCE }*.value.name
        frameworks.containsAll([
                'Apphance-Pre-Production.framework',
                'Security.framework',
                'AudioToolbox.framework',
                'CoreLocation.framework',
                'QuartzCore.framework',
                'SystemConfiguration.framework',
                'CoreTelephony.framework']
        )

        and:
        json.objects.findAll { it.key.length() > 24 }.size() == 14

        cleanup:
        tmpDir.deleteDir()
    }
}
