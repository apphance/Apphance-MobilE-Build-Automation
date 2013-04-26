package com.apphance.ameba.plugins.android.test.tasks

import com.apphance.ameba.configuration.android.AndroidTestConfiguration
import com.apphance.ameba.configuration.properties.BooleanProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.executor.AndroidExecutor
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.executor.command.CommandLogFilesGenerator
import com.apphance.ameba.executor.linker.FileLinker
import spock.lang.Specification

import static com.apphance.ameba.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.ameba.executor.command.CommandLogFilesGenerator.LogFile.STD
import static java.io.File.createTempFile
import static org.gradle.testfixtures.ProjectBuilder.builder

class CreateAVDTaskSpec extends Specification {

    def 'avds dir is created'() {
        given:
        def projectDir = new File('testProjects/android/android-basic')
        def project = builder().withProjectDir(projectDir).build()

        and:
        def fileLinker = Mock(FileLinker) {
            fileLink(_) >> ''
        }

        and:
        def errLog = createTempFile('tmp', 'file-err')
        def outLog = createTempFile('tmp', 'file-out')
        def logFileGenerator = Mock(CommandLogFilesGenerator) {
            commandLogFiles() >> [(STD): outLog, (ERR): errLog]
        }

        and:
        def ce = new CommandExecutor(fileLinker, logFileGenerator)

        and:
        def ae = new AndroidExecutor(ce)

        and:
        def atc = GroovyMock(AndroidTestConfiguration)
        atc.AVDDir >> project.file('avds')
        atc.emulatorName >> 'sampleEmulatorName'
        atc.emulatorTarget >> new StringProperty(value: 'android-7')
        atc.emulatorSkin >> new StringProperty(value: 'WVGA800')
        atc.emulatorCardSize >> new StringProperty(value: '200M')
        atc.emulatorSnapshotEnabled >> new BooleanProperty(value: 'true')

        and:
        def task = project.task(CreateAVDTask.NAME, type: CreateAVDTask) as CreateAVDTask
        task.testConf = atc
        task.androidExecutor = ae

        and:
        new File(projectDir, 'avds').deleteDir()

        when:
        task.createAVD()

        then:
        new File(projectDir, 'avds').exists()

        and:
        [
                'config.ini',
                'sdcard.img',
                'snapshots.img',
                'userdata.img'
        ].every { new File(atc.AVDDir, it).exists() }

        cleanup:
        outLog.delete()
        errLog.delete()
    }
}
