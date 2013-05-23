package com.apphance.ameba.plugins.android.builder

import com.apphance.ameba.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.ameba.executor.AntExecutor
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.executor.command.CommandLogFilesGenerator
import com.apphance.ameba.executor.linker.FileLinker
import com.apphance.ameba.plugins.android.release.AndroidReleaseApkListener
import com.apphance.ameba.plugins.release.AmebaArtifact
import spock.lang.Specification

import static com.apphance.ameba.configuration.ProjectConfiguration.TMP_DIR
import static com.apphance.ameba.configuration.android.AndroidBuildMode.DEBUG
import static com.apphance.ameba.configuration.release.ReleaseConfiguration.OTA_DIR
import static com.apphance.ameba.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.ameba.executor.command.CommandLogFilesGenerator.LogFile.STD
import static java.io.File.createTempFile
import static org.gradle.testfixtures.ProjectBuilder.builder

//TODO WARNING!! THIS TEST WORKS ON AN UPDATED (build.xml, local.properties) PROJECT!!!
class AndroidSingleVariantApkBuilderSpec extends Specification {

    def project = builder().withProjectDir(new File('testProjects/android/android-basic')).build()

    def projectName = 'TestAndroidProject'

    def fileLinker = GroovyStub(FileLinker) {
        fileLink(_) >> ''
    }
    def logFiles = [(STD): createTempFile('tmp', 'file-out'), (ERR): createTempFile('tmp', 'file-err')]
    def logFileGenerator = GroovyStub(CommandLogFilesGenerator) {
        commandLogFiles() >> logFiles
    }
    def executor = new CommandExecutor(fileLinker, logFileGenerator)
    def antExecutor = new AntExecutor()

    def variantsConf = GroovyStub(AndroidVariantsConfiguration) {
        getVariantsDir() >> project.file('variants')
    }

    def variantTmpDir = "${TMP_DIR}/test"

    def builder = new AndroidSingleVariantApkBuilder()

    def setup() {
        antExecutor.executor = executor

        builder.antExecutor = antExecutor
        builder.ant = project.ant
        builder.variantsConf = variantsConf
    }

    def cleanup() {
        logFiles.each {
            it.value.delete()
        }
        project.file(TMP_DIR).deleteDir()
        project.file(OTA_DIR).deleteDir()
    }

    def 'artifacts are built according to passed config'() {
        expect:
        project.file(TMP_DIR).deleteDir()

        and:
        !project.file(TMP_DIR).exists()

        and:
        copyProjectToTmpDir()

        when:
        builder.buildSingle(GroovyStub(AndroidBuilderInfo) {
            getTmpDir() >> project.file(variantTmpDir)
            getVariantDir() >> project.file('variants/test')
            getMode() >> DEBUG
        })

        then:
        def sampleProperties = project.file("${TMP_DIR}/test/res/raw/sample.properties")
        sampleProperties.exists() && sampleProperties.isFile() && sampleProperties.size() > 0

        and:
        [
                "${projectName}-debug.apk",
                "${projectName}-debug-unaligned.apk",
                "${projectName}-debug-unaligned.apk.d",
        ].every { project.file("$variantTmpDir/bin/$it").exists() }
    }

    def 'artifacts are built according to passed config and copied to ota'() {
        given:
        def releaseApk = new File(project.rootDir, "${OTA_DIR}/TestAndroidProject/1.0.1_42/TestAndroidProject-debug-TestDebug-1.0.1_42.apk")
        def artifactProvider = GroovyStub(AndroidArtifactProvider, {
            apkArtifact(_) >> GroovyStub(AmebaArtifact, {
                getLocation() >> releaseApk
            })
        })

        and:
        def listener = new AndroidReleaseApkListener()
        listener.artifactProvider = artifactProvider
        listener.ant = project.ant

        and:
        builder.registerListener(listener)

        expect:
        project.file(TMP_DIR).deleteDir()
        project.file(OTA_DIR).deleteDir()

        and:
        !project.file(TMP_DIR).exists()
        !project.file(OTA_DIR).exists()

        and:
        copyProjectToTmpDir()

        when:
        builder.buildSingle(GroovyStub(AndroidBuilderInfo) {
            getTmpDir() >> project.file(variantTmpDir)
            getVariantDir() >> project.file('variants/test')
            getMode() >> DEBUG
            getOriginalFile() >> project.file("$TMP_DIR/test/bin/TestAndroidProject-debug.apk")
        })

        then:
        def sampleProperties = project.file("$TMP_DIR/test/res/raw/sample.properties")
        sampleProperties.exists() && sampleProperties.isFile() && sampleProperties.size() > 0

        and:
        [
                "${projectName}-debug.apk",
                "${projectName}-debug-unaligned.apk",
                "${projectName}-debug-unaligned.apk.d",
        ].every { project.file("$variantTmpDir/bin/$it").exists() }

        and:
        releaseApk.exists() && releaseApk.isFile()
        !(new File(project.rootDir, "${OTA_DIR}/TestAndroidProject/1.0.1_42/TestAndroidProject-debug-TestDebug-unaligned-1.0.1_42.apk")).exists()
        !(new File(project.rootDir, "${OTA_DIR}/TestAndroidProject/1.0.1_42/TestAndroidProject-debug-TestDebug-unsigned-1.0.1_42.apk")).exists()
    }

    def copyProjectToTmpDir() {
        project.ant.copy(todir: variantTmpDir, failonerror: true, overwrite: true, verbose: true) {
            fileset(dir: project.rootDir.absolutePath + '/') {
                exclude(name: 'variants/**/*')
                exclude(name: 'log/**/*')
                exclude(name: 'bin/**/*')
                exclude(name: 'build/**/*')
            }
        }
    }
}
