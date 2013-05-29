package com.apphance.ameba.plugins.android.builder

import com.apphance.ameba.TestUtils
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.executor.AndroidExecutor
import com.apphance.ameba.executor.AntExecutor
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.executor.command.CommandLogFilesGenerator
import com.apphance.ameba.executor.linker.FileLinker
import com.apphance.ameba.plugins.android.buildplugin.tasks.SingleVariantTask
import com.apphance.ameba.plugins.release.AmebaArtifact
import spock.lang.Specification
import spock.lang.Unroll

import static com.apphance.ameba.configuration.ProjectConfiguration.TMP_DIR
import static com.apphance.ameba.configuration.android.AndroidBuildMode.DEBUG
import static com.apphance.ameba.configuration.release.ReleaseConfiguration.OTA_DIR
import static com.apphance.ameba.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.ameba.executor.command.CommandLogFilesGenerator.LogFile.STD
import static com.apphance.ameba.plugins.android.buildplugin.tasks.UpdateProjectTask.runRecursivelyInAllSubprojects
import static java.io.File.createTempFile
import static org.gradle.testfixtures.ProjectBuilder.builder

@Mixin(TestUtils)
class SingleVariantTaskSpec extends Specification {

    def project = builder().withProjectDir(new File('testProjects/android/android-basic')).build()
    def singleVaraintTask = create SingleVariantTask

    def static projectName = 'TestAndroidProject'
    def static variantTmpDir = "${TMP_DIR}/test"

    def fileLinker = GroovyStub(FileLinker) {
        fileLink(_) >> ''
    }
    def logFiles = [(STD): createTempFile('tmp', 'file-out'), (ERR): createTempFile('tmp', 'file-err')]
    def logFileGenerator = GroovyStub(CommandLogFilesGenerator) {
        commandLogFiles() >> logFiles
    }
    def executor = new CommandExecutor(fileLinker, logFileGenerator)
    def antExecutor = new AntExecutor(executor: executor)
    def androidExecutor = new AndroidExecutor(executor: executor)

    def setup() {
        singleVaraintTask.antExecutor = antExecutor
        singleVaraintTask.ant = project.ant
        singleVaraintTask.androidReleaseConf = Stub(AndroidReleaseConfiguration) { isEnabled() >> true }

        assert project.file(TMP_DIR).deleteDir()
        assert project.file(OTA_DIR).deleteDir()

        runRecursivelyInAllSubprojects(project.rootDir, { File it ->
            File localProps = new File(it, 'local.properties')
            if (!localProps.exists()) {
                androidExecutor.run(it, 'update project -p .')
            }
            assert localProps.exists()
        })
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
        singleVaraintTask.artifactProvider = GroovyStub(AndroidArtifactProvider)
        singleVaraintTask.artifactProvider.artifact(_) >> GroovyStub(AmebaArtifact, {
            getLocation() >> releaseFile
        })

        singleVaraintTask.artifactProvider.builderInfo(_) >> GroovyStub(AndroidBuilderInfo) {
            getTmpDir() >> project.file(variantTmpDir)
            getVariantDir() >> project.file('variants/test')
            getMode() >> DEBUG
            getOriginalFile() >> project.file("$variantTmpDir/bin/$mainArtifact")
        }

        and:
        copyProjectToTmpDir()
        new File(project.file(variantTmpDir), 'project.properties') << "android.library=$library\n"

        when:
        singleVaraintTask.singleVariant()

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
