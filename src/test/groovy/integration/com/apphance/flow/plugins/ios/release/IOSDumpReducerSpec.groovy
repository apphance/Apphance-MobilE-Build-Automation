package com.apphance.flow.plugins.ios.release

import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.executor.command.CommandLogFilesGenerator
import com.apphance.flow.executor.linker.FileLinker
import com.apphance.flow.plugins.ios.parsers.PlistParser
import spock.lang.Shared
import spock.lang.Specification

import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.STD
import static com.google.common.io.Files.createTempDir
import static java.io.File.createTempFile

class IOSDumpReducerSpec extends Specification {

    @Shared emptyDir = createTempDir()
    @Shared emptyFile = createTempFile('empty', 'file')
    @Shared reducer = new IOSDumpReducer()

    @Shared dwarfDumpUUIDOutput = new File(getClass().getResource('dwarfdump_uuid_output').toURI()).readLines().iterator()
    @Shared dwarfDumpArmv7Output = new File(getClass().getResource('dwarfdump_armv7_output').toURI()).readLines().iterator()

    def cleanupSpec() {
        emptyDir.deleteDir()
        emptyFile.delete()
    }

    def 'exception thrown when bad plist passed'() {
        when:
        reducer.validatePlist(plist)

        then:
        def e = thrown(IllegalArgumentException)
        e.message.startsWith('Invalid plist file passed:')

        where:
        plist << [null, emptyDir, emptyFile]
    }

    def 'exception thrown when bad dSYM passed'() {
        when:
        reducer.validatedSYM(dSYMs)

        then:
        def e = thrown(IllegalArgumentException)
        e.message.startsWith('Invalid dSYM dir passed:')

        where:
        dSYMs << [null, emptyDir, emptyFile]
    }

    def 'exception thrown when bad output dir passed'() {
        when:
        reducer.validateOutputDir(outputDir)

        then:
        def e = thrown(IllegalArgumentException)
        e.message.startsWith('Invalid output dir passed:')

        where:
        outputDir << [null, emptyFile]
    }

    def 'exception thrown when bad name passed'() {
        when:
        reducer.validateName(name)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == 'Empty name passed'

        where:
        name << [null, '', ' \t']
    }


    def 'UUID archs are parsed well'() {
        given:
        reducer.executor = GroovyMock(IOSExecutor) {
            dwarfdumpUUID(_) >> dwarfDumpUUIDOutput
        }

        when:
        def UUIDArchs = reducer.UUIDArchs(GroovyMock(File))

        then:
        UUIDArchs.size() == 2
        UUIDArchs.dsymUUID.containsAll(['a75f106ebf423536b65f41b3e944f0cf', 'c8fc05f2a6703b009df09191f0052fe0'])
        UUIDArchs.dsymArch.containsAll(['armv7', 'armv7s'])
    }

    def 'symTable built for architecture'() {
        given:
        reducer.executor = GroovyMock(IOSExecutor) {
            dwarfdumpArch(_, _) >> dwarfDumpArmv7Output
        }

        when:
        def symTable = reducer.symTable(GroovyMock(File), 'armv7')

        then:
        symTable.size() == 40
    }

    def 'ahsym files created'() {
        given:
        def logFiles = [(STD): createTempFile('tmp', 'file-out'), (ERR): createTempFile('tmp', 'file-err')]
        def fileLinker = GroovyMock(FileLinker) {
            fileLink(_) >> ''
        }
        def logFileGenerator = GroovyMock(CommandLogFilesGenerator) {
            commandLogFiles() >> logFiles
        }
        reducer.executor = new IOSExecutor(executor: new CommandExecutor(fileLinker, logFileGenerator))
        and:
        reducer.plistParser = GroovyMock(PlistParser) {
            bundleShortVersionString(_) >> '3145'
            bundleVersion(_) >> '3.1.45'
            bundleId(_) >> 'com.apphance.flow'
            bundleDisplayName(_) >> 'Sample Name'
        }
        and:
        def dSYM = new File(getClass().getResource('GradleXCode.app.dSYM').toURI())

        and:
        def tmpDir = createTempDir()

        and:
        def plist = new File(dSYM, 'Contents/Info.plist')

        when:
        reducer.reduce(plist, dSYM, tmpDir, 'GradleXCode_3.1.45_3145')

        then:
        noExceptionThrown()
        ['GradleXCode_3.1.45_3145_armv7.ahsym', 'GradleXCode_3.1.45_3145_armv7s.ahsym'].every {
            def file = new File(tmpDir, it)
            file.exists() && file.isFile() && file.size() > 0
        }

        cleanup:
        logFiles.each {
            it.value.delete()
        }
        tmpDir.deleteDir()
    }

    def 'list is sorted for key'() {
        expect:
        expected == reducer.sortList(list, key)

        where:
        list             | expected         | key
        [[a: 2], [a: 1]] | [[a: 1], [a: 2]] | 'a'
    }
}
