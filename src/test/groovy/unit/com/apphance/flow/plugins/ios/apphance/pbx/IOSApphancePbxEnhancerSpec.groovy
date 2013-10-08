package com.apphance.flow.plugins.ios.apphance.pbx

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.properties.ApphanceModeProperty
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.executor.command.Command
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.executor.command.CommandLogFilesGenerator
import com.apphance.flow.executor.linker.FileLinker
import com.apphance.flow.plugins.ios.parsers.PbxJsonParser
import groovy.json.JsonSlurper
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
    def pbxJSON = new File('demo/ios/GradleXCode/GradleXCode.xcodeproj/project.pbxproj.json')

    def 'basic pbx json objects found'() {
        given:
        def enhancer = new IOSApphancePbxEnhancer(GroovyMock(AbstractIOSVariant) {
            getTarget() >> 'GradleXCode'
            getArchiveConfiguration() >> 'Release'
        })
        enhancer.executor = GroovyMock(IOSExecutor) {
            pbxProjToJSON(_) >> pbxJSON.text.split('\n')
        }

        expect:
        enhancer.rootObject.isa == 'PBXProject'
        enhancer.target.name == 'GradleXCode'
        enhancer.configurations*.name == ['Release']
        enhancer.frameworksBuildPhase.files.containsAll(
                'D382B71614703FE500E9CC9B', 'D382B71814703FE500E9CC9B', 'D382B71A14703FE500E9CC9B'
        )
        enhancer.mainGroup.children.containsAll(
                'D382B71B14703FE500E9CC9B', 'D382B73C14703FE500E9CC9B', 'D382B71414703FE500E9CC9B', 'D382B71214703FE500E9CC9B'
        )
        enhancer.GCCPrefixFilePaths == ['GradleXCode/GradleXCode-Prefix.pch']
        enhancer.filesToReplaceLogs.size() > 0
        enhancer.mainGroupFrameworks.name == 'Frameworks'
    }

    def 'apphance is added to pbx'() {
        given:
        def tmpDir = createTempDir()

        and:
        def conf = GroovyStub(IOSConfiguration) {
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
        def variant = GroovyMock(AbstractIOSVariant) {
            getTarget() >> 'GradleXCode'
            getArchiveConfiguration() >> 'Release'
            getApphanceMode() >> new ApphanceModeProperty(value: QA)
            getTmpDir() >> tmpDir
            getPbxFile() >> new File(tmpDir, 'GradleXCode/GradleXCode.xcodeproj/project.pbxproj')
        }
        and:
        def enhancer = new IOSApphancePbxEnhancer(variant)
        enhancer.executor = executor
        enhancer.pbxJsonParser = new PbxJsonParser(executor: executor)

        and:
        new File(tmpDir, 'GradleXCode/GradleXCode.xcodeproj').mkdirs()

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
        def pbxJson = commandExecutor.executeCommand(new Command(runDir: pbx.parentFile, cmd: ['plutil', '-convert', 'json', pbx, '-o', '-']))

        then:
        def json = new JsonSlurper().parseText(pbxJson.join('\n')) as Map
        def rootObject = json.objects[json.rootObject] as Map
        def target = json.objects.find {
            it.key in rootObject.targets && it.value.isa == PBX_NATIVE_TARGET && it.value.name == variant.target
        }.value as Map
        def confHashes = json.objects[target.buildConfigurationList].buildConfigurations
        def archiveConfiguration = json.objects.find {
            it.key in confHashes && it.value.isa == XCBUILD_CONFIGURATION && it.value.name == variant.archiveConfiguration
        }.value as Map

        and:
        archiveConfiguration.buildSettings.OTHER_LDFLAGS == ['-ObjC', '-all_load']
        archiveConfiguration.buildSettings.FRAMEWORK_SEARCH_PATHS == ['$(inherited)', '"$(SRCROOT)"']
        archiveConfiguration.buildSettings.LIBRARY_SEARCH_PATHS == ['$(inherited)', "\"\$(SRCROOT)/Apphance-Pre-Production.framework\""]

        and:
        def frameworks = json.objects.findAll { it.value.isa == PBX_FILE_REFERENCE }*.value.name
        frameworks.containsAll([
                'Apphance-Pre-Production.framework',
                'Security.framework',
                'AudioToolbox.framework',
                'CoreLocation.framework',
                'QuartzCore.framework',
                'SystemConfiguration.framework',
                'CoreTelephony.framework',
                'AssetsLibrary.framework',
        ])

        and:
        json.objects.findAll { it.key.length() > 24 }.size() == 16

        cleanup:
        tmpDir.deleteDir()
    }
}
