package com.apphance.ameba.applyPlugins.android

import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.android.*
import com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin
import org.gradle.api.Project

import static org.junit.Assert.assertEquals

class AndroidSingleVariantBuilderTest extends BaseAndroidTaskTest {

    private AndroidSingleVariantApkBuilder prepareEnvironment(Project project) {
        use(PropertyCategory) {
            AndroidManifestHelper manifestHelper = new AndroidManifestHelper()
            manifestHelper.readVersion(project.rootDir, project.getProjectConfiguration())
            AndroidBuildXmlHelper buildXmlHelper = new AndroidBuildXmlHelper()
            Properties props = new Properties()
            props.load(new FileInputStream(project.file("gradle.properties")))
            props.keys().each { key ->
                project[key] = props.getProperty(key)
            }
            project.ext[ProjectConfigurationPlugin.PROJECT_NAME_PROPERTY] = buildXmlHelper.readProjectName(project.rootDir)
            project.retrieveBasicProjectData()
            AndroidSingleVariantApkBuilder builder = new AndroidSingleVariantApkBuilder(project,
                    AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration(project))
            builder.updateAndroidConfigurationWithVariants()
            AndroidProjectConfigurationRetriever.readAndroidProjectConfiguration(project)
            return builder
        }
    }

    public void testArtifactBuilderInfoVariantedDebug() throws Exception {
        Project project = getProject()
        AndroidSingleVariantApkBuilder builder = prepareEnvironment(project)
        AndroidBuilderInfo ai = builder.buildApkArtifactBuilderInfo("test", null)
        assertEquals('test', ai.variant)
        assertEquals('Debug', ai.debugRelease)
        assertEquals(new File("testProjects/android/tmp-android-basic-test/bin").absolutePath, ai.buildDirectory.absolutePath)
        assertEquals(new File("testProjects/android/tmp-android-basic-test/bin/TestAndroidProject-debug.apk").absolutePath, ai.originalFile.absolutePath)
        assertEquals('TestAndroidProject-debug-test-1.0.1_42', ai.fullReleaseName)
        assertEquals('TestAndroidProject-debug-test-1.0.1_42', ai.filePrefix)
    }

    public void testArtifactBuilderInfoVariantedRelease() throws Exception {
        Project project = getProject()
        AndroidSingleVariantApkBuilder builder = prepareEnvironment(project)
        AndroidBuilderInfo ai = builder.buildApkArtifactBuilderInfo("market", null)
        assertEquals('market', ai.variant)
        assertEquals('Release', ai.debugRelease)
        assertEquals(new File("testProjects/android/tmp-android-basic-market/bin").absolutePath, ai.buildDirectory.absolutePath)
        assertEquals(new File("testProjects/android/tmp-android-basic-market/bin/TestAndroidProject-release.apk").absolutePath, ai.originalFile.absolutePath)
        assertEquals('TestAndroidProject-release-market-1.0.1_42', ai.fullReleaseName)
        assertEquals('TestAndroidProject-release-market-1.0.1_42', ai.filePrefix)
    }

    public void testArtifactBuilderInfoNotVariantedDebug() throws Exception {
        Project project = getProject(false)
        AndroidSingleVariantApkBuilder builder = prepareEnvironment(project)
        AndroidBuilderInfo ai = builder.buildApkArtifactBuilderInfo("Debug", "Debug")
        assertEquals('Debug', ai.variant)
        assertEquals('Debug', ai.debugRelease)
        assertEquals(new File("testProjects/android/tmp-android-novariants-Debug/bin").absolutePath, ai.buildDirectory.absolutePath)
        assertEquals(new File("testProjects/android/tmp-android-novariants-Debug/bin/TestAndroidProject-debug.apk").absolutePath, ai.originalFile.absolutePath)
        assertEquals('TestAndroidProject-debug-Debug-1.0.1_42', ai.fullReleaseName)
        assertEquals('TestAndroidProject-debug-Debug-1.0.1_42', ai.filePrefix)
    }

    public void testArtifactBuilderInfoNotVariantedRelease() throws Exception {
        Project project = getProject(false)
        AndroidSingleVariantApkBuilder builder = prepareEnvironment(project)
        AndroidBuilderInfo ai = builder.buildApkArtifactBuilderInfo("Release", "Release")
        assertEquals('Release', ai.variant)
        assertEquals('Release', ai.debugRelease)
        assertEquals(new File("testProjects/android/tmp-android-novariants-Release/bin").absolutePath, ai.buildDirectory.absolutePath)
        assertEquals(new File("testProjects/android/tmp-android-novariants-Release/bin/TestAndroidProject-release.apk").absolutePath, ai.originalFile.absolutePath)
        assertEquals('TestAndroidProject-release-Release-1.0.1_42', ai.fullReleaseName)
        assertEquals('TestAndroidProject-release-Release-1.0.1_42', ai.filePrefix)
    }
}
