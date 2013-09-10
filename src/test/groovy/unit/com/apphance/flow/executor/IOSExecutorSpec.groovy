package com.apphance.flow.executor

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.executor.command.CommandLogFilesGenerator
import com.apphance.flow.executor.linker.FileLinker
import com.apphance.flow.plugins.ios.parsers.XCodeOutputParser
import groovy.json.JsonSlurper
import org.gradle.api.Project
import spock.lang.Specification

import static com.apphance.flow.configuration.ios.IOSConfiguration.PROJECT_PBXPROJ
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.STD
import static java.io.File.createTempFile

class IOSExecutorSpec extends Specification {

    def fileLinker = Mock(FileLinker)
    def logFileGenerator = Mock(CommandLogFilesGenerator)

    def executor = new CommandExecutor(fileLinker, logFileGenerator)

    def logFiles = [(STD): createTempFile('tmp', 'file-out'), (ERR): createTempFile('tmp', 'file-err')]

    def conf

    def iosExecutor = new IOSExecutor()

    def setup() {
        fileLinker.fileLink(_) >> ''
        logFileGenerator.commandLogFiles() >> logFiles

        conf = GroovySpy(IOSConfiguration)
        conf.project = GroovyStub(Project) {
            getRootDir() >> new File('testProjects/ios/GradleXCode')
        }
        conf.xcodeDir >> new FileProperty(value: new File('GradleXCode.xcodeproj'))

        iosExecutor.executor = executor
        iosExecutor.conf = conf
        iosExecutor.parser = new XCodeOutputParser()
    }

    def cleanup() {
        logFiles.each {
            it.value.delete()
        }
    }

    def 'pbxproj is converted to json format well'() {
        when:
        def json = iosExecutor.pbxProjToJSON(new File("$conf.rootDir.absolutePath/$conf.xcodeDir.value.name", PROJECT_PBXPROJ))

        then:
        noExceptionThrown()

        and:
        def slurped = new JsonSlurper().parseText(json.join('\n'))
        slurped.objectVersion == '46'
        slurped.archiveVersion == '1'
    }

    def 'plist is converted to json format well'() {
        when:
        def json = iosExecutor.plistToJSON(new File(getClass().getResource('Test.plist').toURI()))
        json = json.join('\n')

        then:
        noExceptionThrown()

        and:
        def slurped = new JsonSlurper().parseText(json)
        slurped.CFBundleName == 'Some'
        slurped.CFBundleIdentifier == 'com.apphance.flow'
    }

    def 'build settings got for target and configuration'() {
        when:
        def settings = iosExecutor.buildSettings('GradleXCode', 'BasicConfiguration')

        then:
        settings.size() > 0
        settings.keySet().every { it.matches('([A-Z0-9a-z]+_)*([A-Z0-9a-z])+') }
    }

    def 'runs dot clean'() {
        given:
        def ce = GroovyMock(CommandExecutor)

        and:
        def iose = new IOSExecutor(executor: ce, conf: GroovySpy(IOSConfiguration) {
            getRootDir() >> new File('sampleDir')
        })

        when:
        iose.clean()

        then:
        1 * ce.executeCommand({ it.commandForExecution.join(' ') == 'dot_clean ./' && it.runDir.name == 'sampleDir' })
    }

    def 'mobileprovision is converted to xml well'() {
        when:
        def xml = iosExecutor.mobileprovisionToXml(
                new File(conf.rootDir, 'release/distribution_resources/GradleXCode.mobileprovision'))

        then:
        xml.join('\n') == new File(getClass().getResource('GradleXCode.mobileprovision.xml').toURI()).text
    }

    def 'xCode version is read correctly'() {
        expect:
        iosExecutor.xCodeVersion.matches('(\\d+\\.)+\\d+')
    }

    def 'ios-sim version is read correctly'() {
        expect:
        iosExecutor.iOSSimVersion.matches('(\\d+\\.)+\\d+')
    }

    def 'ios-sim version empty when ios-sim not installed'() {
        given:
        iosExecutor.executor = GroovyMock(CommandExecutor) {
            executeCommand(_) >> { throw new Exception('no ios-sim') }
        }

        expect:
        iosExecutor.iOSSimVersion == ''
    }

    def 'archive command is executed well'() {
        given:
        def ce = GroovyMock(CommandExecutor)

        and:
        def rootDir = new File('rootDir')

        and:
        def iose = new IOSExecutor(executor: ce, conf: GroovySpy(IOSConfiguration) {
            getRootDir() >> rootDir
        })

        when:
        iose.buildVariant(rootDir, ['xcodebuild', '-project', 'Sample.xcodeproj', '-scheme', 's1', 'clean', 'archive'])

        then:
        1 * ce.executeCommand({ it.commandForExecution.join(' ') == 'xcodebuild -project Sample.xcodeproj -scheme s1 clean archive' && it.runDir.name == 'rootDir' })

        cleanup:
        rootDir.delete()
    }

    def 'running tests is executed well'() {
        given:
        def ce = GroovyMock(CommandExecutor)

        and:
        def rootDir = new File('rootDir')
        def conf = GroovySpy(IOSConfiguration) {
            getRootDir() >> rootDir
            getXcodebuildExecutionPath() >> ['xcodebuild', '-project', 'Sample.xcodeproj']
        }

        and:
        def iose = new IOSExecutor(executor: ce, conf: conf)
        def cmd = conf.xcodebuildExecutionPath + ['-target', 't1', '-configuration', 'c1', '-sdk', 'iphonesimulator', 'clean', 'build']

        when:
        iose.runTests(rootDir, cmd, 'somePath')

        then:
        1 * ce.executeCommand(
                {
                    it.commandForExecution.join(' ') == 'xcodebuild -project Sample.xcodeproj -target t1 -configuration c1 -sdk iphonesimulator clean build' &&
                            it.runDir.name == 'rootDir' &&
                            it.failOnError == false &&
                            it.environment.RUN_UNIT_TEST_WITH_IOS_SIM == 'YES' &&
                            it.environment.UNIT_TEST_OUTPUT_FILE == 'somePath'
                })

        cleanup:
        rootDir.delete()
    }

    def 'dwarfdump arch is executed well'() {
        given:
        def ce = GroovyMock(CommandExecutor)

        and:
        def dSYM = new File('dSYM')

        and:
        def iose = new IOSExecutor(executor: ce)

        when:
        iose.dwarfdumpArch(dSYM, 'armv7')

        then:
        1 * ce.executeCommand({
            it.commandForExecution.join(' ') == "dwarfdump --arch armv7 ${dSYM.absolutePath}" &&
                    it.runDir == dSYM.parentFile
        })

        cleanup:
        dSYM.delete()
    }

    def 'dwarfdump UUID is executed well'() {
        given:
        def ce = GroovyMock(CommandExecutor)

        and:
        def dSYM = new File('dSYM')

        and:
        def iose = new IOSExecutor(executor: ce)

        when:
        iose.dwarfdumpUUID(dSYM)

        then:
        1 * ce.executeCommand({
            it.commandForExecution.join(' ') == "dwarfdump -u ${dSYM.absolutePath}" &&
                    it.runDir == dSYM.parentFile
        })

        cleanup:
        dSYM.delete()

    }
}
