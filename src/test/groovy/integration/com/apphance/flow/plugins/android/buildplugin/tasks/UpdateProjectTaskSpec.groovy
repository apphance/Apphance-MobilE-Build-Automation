package com.apphance.flow.plugins.android.buildplugin.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
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

class UpdateProjectTaskSpec extends Specification {

    def 'all projects are updated'() {
        given:
        def testProjectDir = new File('testProjects/android/android-basic')

        and:
        def props = [
                new File(testProjectDir, 'local.properties'),
                new File(testProjectDir, 'subproject/local.properties'),
                new File(testProjectDir, 'subproject/subsubproject/local.properties')
        ]

        and:
        def project = builder().withProjectDir(testProjectDir).build()

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
        def ac = GroovySpy(AndroidConfiguration)
        ac.target >> new StringProperty(value: 'android-7')
        ac.projectName >> new StringProperty(value: 'TestAndroidProject')
        ac.project = GroovyStub(Project) {
            getRootDir() >> project.rootDir
        }

        and:
        def ae = new AndroidExecutor(executor: ce, conf: ac, executableAndroid: STD_EXECUTABLE_ANDROID)

        and:
        def pu = new AndroidProjectUpdater(executor: ae, conf: ac)

        and:
        def updateTask = project.task(UpdateProjectTask.NAME, type: UpdateProjectTask) as UpdateProjectTask
        updateTask.projectUpdater = pu
        updateTask.conf = ac

        when:
        props.each { it.delete() }
        updateTask.runUpdate()

        then:
        props.every { it.exists() }

        cleanup:
        outLog.delete()
        errLog.delete()
    }
}
