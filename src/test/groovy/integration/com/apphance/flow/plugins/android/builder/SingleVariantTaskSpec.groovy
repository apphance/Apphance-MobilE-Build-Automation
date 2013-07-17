package com.apphance.flow.plugins.android.builder

import com.apphance.flow.TestUtils
import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.AndroidExecutor
import com.apphance.flow.executor.AntExecutor
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.executor.command.CommandLogFilesGenerator
import com.apphance.flow.executor.linker.FileLinker
import com.apphance.flow.plugins.android.buildplugin.tasks.AndroidProjectUpdater
import com.apphance.flow.plugins.android.buildplugin.tasks.SingleVariantTask
import com.apphance.flow.plugins.release.FlowArtifact
import spock.lang.Specification
import spock.lang.Unroll

import static com.apphance.flow.configuration.ProjectConfiguration.TMP_DIR
import static com.apphance.flow.configuration.android.AndroidBuildMode.DEBUG
import static com.apphance.flow.configuration.release.ReleaseConfiguration.OTA_DIR
import static com.apphance.flow.executor.ExecutableCommand.STD_EXECUTABLE_ANDROID
import static com.apphance.flow.executor.ExecutableCommand.STD_EXECUTABLE_ANT
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.STD
import static java.io.File.createTempFile
import static org.gradle.testfixtures.ProjectBuilder.builder

@Mixin(TestUtils)
class SingleVariantTaskSpec extends Specification {

    def project = builder().withProjectDir(new File('testProjects/android/android-basic')).build()
    def task = create SingleVariantTask

    def static projectName = 'TestAndroidProject'
    def static variantTmpDir = "${TMP_DIR}/test"

    def fileLinker = GroovyStub(FileLinker) {
        fileLink(_) >> ''
    }
    def logFiles = [(STD): createTempFile('tmp', 'file-out'), (ERR): createTempFile('tmp', 'file-err')]
    def logFileGenerator = GroovyStub(CommandLogFilesGenerator) {
        commandLogFiles() >> logFiles
    }
    def conf = GroovyStub(AndroidConfiguration) {
        getTarget() >> new StringProperty(value: 'android-7')
        getProjectName() >> new StringProperty(value: projectName)
        getRootDir() >> project.rootDir
    }
    def executor = new CommandExecutor(fileLinker, logFileGenerator)
    def antExecutor = new AntExecutor(executor: executor, executableAnt: STD_EXECUTABLE_ANT)
    def androidExecutor = new AndroidExecutor(executor: executor, conf: conf, executableAndroid: STD_EXECUTABLE_ANDROID)
    def projectUpdater = new AndroidProjectUpdater(conf: conf, executor: androidExecutor)

    def setup() {
        task.antExecutor = antExecutor
        task.projectUpdater = projectUpdater
        task.ant = project.ant
        task.releaseConf = Stub(AndroidReleaseConfiguration) { isEnabled() >> true }
        task.variant = GroovyMock(AndroidVariantConfiguration) {
            getTmpDir() >> new File(project.rootDir, variantTmpDir.toString())
            getOldPackage() >> new StringProperty()
            getNewPackage() >> new StringProperty()
        }

        assert project.file(TMP_DIR).deleteDir()
        assert project.file(OTA_DIR).deleteDir()
    }

    def cleanup() {
        logFiles.each {
            it.value.delete()
        }
        project.file(TMP_DIR).deleteDir()
        project.file(OTA_DIR).deleteDir()
    }

    @Unroll
    def 'artifacts are built according to passed config and copied to ota dir. Library = #library'() {
        given:
        def releaseFile = new File(project.rootDir, "${OTA_DIR}/TestAndroidProject/1.0.1_42/TestAndroidProject-debug-TestDebug-1.0.1_42.$releaseFileExtension")
        task.artifactProvider = GroovyStub(AndroidArtifactProvider)
        task.artifactProvider.artifact(_) >> GroovyStub(FlowArtifact, {
            getLocation() >> releaseFile
        })

        task.artifactProvider.builderInfo(_) >> GroovyStub(AndroidBuilderInfo) {
            getTmpDir() >> project.file(variantTmpDir)
            getVariantDir() >> project.file('variants/test')
            getMode() >> DEBUG
            getOriginalFile() >> project.file("$variantTmpDir/bin/$mainArtifact")
        }

        and:
        copyProjectToTmpDir()
        new File(project.file(variantTmpDir), 'project.properties') << "android.library=$library\n"

        when:
        task.singleVariant()

        then: 'files from variant directory are copied to tmp directory'
        def sampleProperties = project.file("${TMP_DIR}/test/res/raw/sample.properties")
        sampleProperties.isFile() && sampleProperties.length()

        and: 'bin directory has all required artifacts'
        (otherOutputs + mainArtifact).every { project.file("$variantTmpDir/bin/$it").exists() }

        and: 'ota directory has only one file: generated artifact'
        releaseFile.isFile()
        releaseFile.parentFile.listFiles().size() == 1

        where:
        library | releaseFileExtension | mainArtifact               | otherOutputs
        false   | 'apk'                | "${projectName}-debug.apk" | ["${projectName}-debug-unaligned.apk", "${projectName}-debug-unaligned.apk.d"]
        true    | 'jar'                | "classes.jar"              | []
    }

    def copyProjectToTmpDir() {
        project.ant.copy(todir: variantTmpDir, failonerror: true, overwrite: true, verbose: true) {
            fileset(dir: project.rootDir.absolutePath + '/') {
                ['variants', 'log', 'bin', 'build'].each {
                    exclude(name: "$it/**/*")
                }
            }
        }
    }
}
