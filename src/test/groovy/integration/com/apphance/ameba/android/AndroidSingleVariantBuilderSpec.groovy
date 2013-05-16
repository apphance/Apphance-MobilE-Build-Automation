package com.apphance.ameba.android

import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.executor.command.CommandLogFilesGenerator
import com.apphance.ameba.executor.linker.SimpleFileLinker
import com.apphance.ameba.plugins.AmebaPlugin
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

@Ignore('to rewrite..')
class AndroidSingleVariantBuilderSpec extends Specification {

    @Shared
    def executor = new CommandExecutor(
            new SimpleFileLinker(),
            new CommandLogFilesGenerator(new File(System.properties['java.io.tmpdir'].toString(), "log${System.currentTimeMillis()}")))

    @Shared
    def projectSimple

    @Shared
    def projectWithoutVariants

    @Shared
    def builderProjectSimple

    @Shared
    def builderProjectWithoutVariants

    def setupSpec() {

        ProjectBuilder projectBuilder = ProjectBuilder.builder()

        projectBuilder.withProjectDir(new File('testProjects/android/android-basic'))
        projectSimple = projectBuilder.build()
        projectSimple.project.plugins.apply(AmebaPlugin)

        projectBuilder.withProjectDir(new File('testProjects/android/android-novariants'))
        projectWithoutVariants = projectBuilder.build()
        projectWithoutVariants.project.plugins.apply(AmebaPlugin)

        builderProjectSimple = createBuilder(projectSimple)
        builderProjectWithoutVariants = createBuilder(projectWithoutVariants)
    }

    def createBuilder(Project project) {
//        use(PropertyCategory) {
//            def manifestHelper = new AndroidManifestHelper()
//            project.getProjectConfiguration().updateVersionDetails(manifestHelper.readVersion(project.rootDir))
//            def buildXmlHelper = new AndroidBuildXmlHelper()
//            def props = new Properties()
//            props.load(new FileInputStream(project.file('gradle.properties')))
//            props.keys().each { key ->
//                project[key] = props.getProperty(key)
//            }
//            project.ext[PropertyCategory.PROJECT_NAME_PROPERTY] = buildXmlHelper.projectName(project.rootDir)
//            project.retrieveBasicProjectData()
//            def builder = new AndroidSingleVariantApkBuilder(* [null] * 2)
//            return builder
//        }
        null
    }

    def 'builds varianted debug'() {
        when:
        def ai = builderProjectSimple.buildApkArtifactBuilderInfo('test', null)

        then:
        'test' == ai.variant
        new File("testProjects/android/tmp-android-basic-test/bin").absolutePath == ai.buildDir.absolutePath
        new File("testProjects/android/tmp-android-basic-test/bin/TestAndroidProject-debug.apk").absolutePath == ai.originalFile.absolutePath
        'TestAndroidProject-debug-test-1.0.1_42' == ai.fullReleaseName
        'TestAndroidProject-debug-test-1.0.1_42' == ai.filePrefix
    }

    def 'builds varianted release'() {
        when:
        def ai = builderProjectSimple.buildApkArtifactBuilderInfo("market", null)

        then:
        'market' == ai.variant
        'Release' == ai.mode
        new File("testProjects/android/tmp-android-basic-market/bin").absolutePath == ai.buildDir.absolutePath
        new File("testProjects/android/tmp-android-basic-market/bin/TestAndroidProject-release.apk").absolutePath == ai.originalFile.absolutePath
        'TestAndroidProject-release-market-1.0.1_42' == ai.fullReleaseName
        'TestAndroidProject-release-market-1.0.1_42' == ai.filePrefix
    }

    def 'builds not varianted debug'() {
        when:
        def ai = builderProjectWithoutVariants.buildApkArtifactBuilderInfo("Debug", "Debug")

        then:
        'Debug' == ai.variant
        'Debug' == ai.mode
        new File("testProjects/android/tmp-android-novariants-Debug/bin").absolutePath == ai.buildDir.absolutePath
        new File("testProjects/android/tmp-android-novariants-Debug/bin/TestAndroidProject-debug.apk").absolutePath == ai.originalFile.absolutePath
        'TestAndroidProject-debug-Debug-1.0.1_42' == ai.fullReleaseName
        'TestAndroidProject-debug-Debug-1.0.1_42' == ai.filePrefix
    }

    def 'build not varianted release'() {
        when:
        def ai = builderProjectWithoutVariants.buildApkArtifactBuilderInfo("Release", "Release")

        then:
        'Release' == ai.variant
        'Release' == ai.mode
        new File("testProjects/android/tmp-android-novariants-Release/bin").absolutePath == ai.buildDir.absolutePath
        new File("testProjects/android/tmp-android-novariants-Release/bin/TestAndroidProject-release.apk").absolutePath == ai.originalFile.absolutePath
        'TestAndroidProject-release-Release-1.0.1_42' == ai.fullReleaseName
        'TestAndroidProject-release-Release-1.0.1_42' == ai.filePrefix
    }

}
