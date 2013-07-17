package com.apphance.flow.plugins.android.test.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidTestConfiguration
import com.apphance.flow.configuration.properties.BooleanProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.AndroidExecutor
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.executor.command.CommandLogFilesGenerator
import com.apphance.flow.executor.linker.FileLinker
import org.gradle.api.Project
import spock.lang.Specification

import static com.apphance.flow.executor.ExecutableCommand.STD_EXECUTABLE_ANDROID
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.STD
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
        def ac = new AndroidConfiguration(project: GroovyStub(Project) {
            getRootDir() >> project.rootDir
        })

        and:
        def ae = new AndroidExecutor(executor: ce, conf: ac, executableAndroid: STD_EXECUTABLE_ANDROID)


        and:
        def atc = GroovyMock(AndroidTestConfiguration)
        atc.AVDDir >> project.file('avds')
        atc.emulatorName >> 'sampleEmulatorName'
        atc.emulatorTarget >> new StringProperty(value: 'android-7')
        atc.emulatorSkin >> new StringProperty(value: 'WVGA800')
        atc.emulatorCardSize >> new StringProperty(value: '9M')
        atc.emulatorSnapshotEnabled >> new BooleanProperty(value: 'true')

        and:
        def task = project.task(CreateAVDTask.NAME, type: CreateAVDTask) as CreateAVDTask
        task.conf = ac
        task.testConf = atc
        task.androidExecutor = ae

        and:
        def avdsDir = new File(projectDir, 'avds')

        and:
        avdsDir.deleteDir()

        when:
        task.createAVD()

        then:
        avdsDir.exists()

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
        avdsDir.deleteDir()
    }
}
