package com.apphance.ameba.executor

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.executor.command.CommandLogFilesGenerator
import com.apphance.ameba.executor.linker.FileLinker
import groovy.json.JsonSlurper
import spock.lang.Specification

import static com.apphance.ameba.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.ameba.executor.command.CommandLogFilesGenerator.LogFile.STD
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

        conf = GroovyMock(IOSConfiguration)
        conf.rootDir >> new File('testProjects/ios/GradleXCode')
        conf.xcodeDir >> new FileProperty(value: new File('GradleXCode.xcodeproj'))

        iosExecutor.commandExecutor = executor
        iosExecutor.conf = conf
    }

    def cleanup() {
        logFiles.each {
            it.value.delete()
        }
    }

    def 'pbxproj is converted to xml format well'() {
        when:
        def xml = iosExecutor.pbxProjToXml()
        xml = xml.join('\n')

        then:
        noExceptionThrown()

        and:
        xml.startsWith('<?xml version="1.0" encoding="UTF-8"?>')

        and:
        def slurped = new XmlSlurper().parse(new ByteArrayInputStream(xml.bytes))
        slurped.dict.dict[1].key[0].text() == '6799F9CB151CA7A700178017'
    }

    def 'pbxproj is converted to json format well'() {
        when:
        def json = iosExecutor.pbxProjToJSON()
        json = json.join('\n')

        then:
        noExceptionThrown()

        and:
        def slurped = new JsonSlurper().parseText(json)
        slurped.objectVersion == '46'
        slurped.archiveVersion == '1'
    }

    def 'plist is converted to json format well'() {
        when:
        def json = iosExecutor.plistToJSON(new File(conf.rootDir, "GradleXCode/GradleXCode-Info.plist"))
        json = json.join('\n')

        then:
        noExceptionThrown()

        and:
        def slurped = new JsonSlurper().parseText(json)
        slurped.CFBundleName == '${PRODUCT_NAME}'
        slurped.CFBundleIdentifier == 'com.apphance.ameba'
    }
}
