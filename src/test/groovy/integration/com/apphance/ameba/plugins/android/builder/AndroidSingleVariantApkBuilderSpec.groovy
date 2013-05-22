package com.apphance.ameba.plugins.android.builder

import com.apphance.ameba.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.ameba.executor.AntExecutor
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.executor.command.CommandLogFilesGenerator
import com.apphance.ameba.executor.linker.FileLinker
import com.apphance.ameba.plugins.android.release.AndroidReleaseApkListener
import com.apphance.ameba.plugins.release.AmebaArtifact
import spock.lang.Specification

import static com.apphance.ameba.configuration.android.AndroidBuildMode.DEBUG
import static com.apphance.ameba.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.ameba.executor.command.CommandLogFilesGenerator.LogFile.STD
import static java.io.File.createTempFile
import static org.gradle.testfixtures.ProjectBuilder.builder

class AndroidSingleVariantApkBuilderSpec extends Specification {

    def p = builder().withProjectDir(new File('testProjects/android/android-basic')).build()

    def projectName = 'TestAndroidProject'

    def fileLinker = GroovyMock(FileLinker, {
        fileLink(_) >> ''
    })
    def logFiles = [(STD): createTempFile('tmp', 'file-out'), (ERR): createTempFile('tmp', 'file-err')]
    def logFileGenerator = GroovyMock(CommandLogFilesGenerator, {
        commandLogFiles() >> logFiles
    })
    def executor = new CommandExecutor(fileLinker, logFileGenerator)

    def antExecutor = new AntExecutor()

    def variantsConf = GroovyMock(AndroidVariantsConfiguration, {
        getVariantsDir() >> p.file('variants')
    })

    def variantTmpDir = 'ameba-tmp/test'

    def builder = new AndroidSingleVariantApkBuilder()

    def setup() {
        antExecutor.executor = executor

        builder.antExecutor = antExecutor
        builder.ant = p.ant
        builder.variantsConf = variantsConf
    }

    def cleanup() {
        logFiles.each {
            it.value.delete()
        }
        p.file('ameba-tmp').deleteDir()
        p.file('ameba-ota').deleteDir()
    }

    def 'artifacts are built according to passed config'() {
        expect:
        p.file('ameba-tmp').deleteDir()

        and:
        !p.file('ameba-tmp').exists()

        and:
        copyProjectToTmpDir()

        when:
        builder.buildSingle(GroovyMock(AndroidBuilderInfo) {
            getTmpDir() >> p.file(variantTmpDir)
            getVariantDir() >> p.file('variants/test')
            getMode() >> DEBUG
        })

        then:
        def sampleProperties = p.file('ameba-tmp/test/res/raw/sample.properties')
        sampleProperties.exists() && sampleProperties.isFile() && sampleProperties.size() > 0

        and:
        [
                "${projectName}-debug.apk",
                "${projectName}-debug-unaligned.apk",
                "${projectName}-debug-unaligned.apk.d",
        ].every { p.file("$variantTmpDir/bin/$it").exists() }
    }

    def 'artifacts are built according to passed config and copied to ota'() {
        given:
        def releaseApk = new File(p.rootDir, 'ameba-ota/TestAndroidProject/1.0.1_42/TestAndroidProject-debug-TestDebug-1.0.1_42.apk')
        def artifactProvider = GroovyMock(AndroidArtifactProvider, {
            apkArtifact(_) >> GroovyMock(AmebaArtifact, {
                getLocation() >> releaseApk
            })
        })

        and:
        def listener = new AndroidReleaseApkListener()
        listener.artifactProvider = artifactProvider
        listener.ant = p.ant

        and:
        builder.registerListener(listener)

        expect:
        p.file('ameba-tmp').deleteDir()
        p.file('ameba-ota').deleteDir()

        and:
        !p.file('ameba-tmp').exists()
        !p.file('ameba-ota').exists()

        and:
        copyProjectToTmpDir()

        when:
        builder.buildSingle(GroovyMock(AndroidBuilderInfo) {
            getTmpDir() >> p.file(variantTmpDir)
            getVariantDir() >> p.file('variants/test')
            getMode() >> DEBUG
            getOriginalFile() >> p.file('ameba-tmp/test/bin/TestAndroidProject-debug.apk')
        })

        then:
        def sampleProperties = p.file('ameba-tmp/test/res/raw/sample.properties')
        sampleProperties.exists() && sampleProperties.isFile() && sampleProperties.size() > 0

        and:
        [
                "${projectName}-debug.apk",
                "${projectName}-debug-unaligned.apk",
                "${projectName}-debug-unaligned.apk.d",
        ].every { p.file("$variantTmpDir/bin/$it").exists() }

        and:
        releaseApk.exists() && releaseApk.isFile()
        !(new File(p.rootDir, 'ameba-ota/TestAndroidProject/1.0.1_42/TestAndroidProject-debug-TestDebug-unaligned-1.0.1_42.apk')).exists()
        !(new File(p.rootDir, 'ameba-ota/TestAndroidProject/1.0.1_42/TestAndroidProject-debug-TestDebug-unsigned-1.0.1_42.apk')).exists()
    }

    def copyProjectToTmpDir() {
        p.ant.copy(todir: variantTmpDir, failonerror: true, overwrite: true, verbose: true) {
            fileset(dir: p.rootDir.absolutePath + '/') {
                exclude(name: 'variants/**/*')
                exclude(name: 'log/**/*')
                exclude(name: 'bin/**/*')
                exclude(name: 'build/**/*')
            }
        }
    }
}
