package com.apphance.ameba.applyPlugins.android;

import static org.junit.Assert.*

import org.gradle.api.Project

import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.android.AndroidArtifactBuilderInfo
import com.apphance.ameba.android.AndroidBuildXmlHelper
import com.apphance.ameba.android.AndroidManifestHelper
import com.apphance.ameba.android.AndroidProjectConfigurationRetriever
import com.apphance.ameba.android.AndroidSingleVariantBuilder
import com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin

class AndroidSingleVariantBuilderTest extends BaseAndroidTaskTest {

    private AndroidSingleVariantBuilder prepareEnvironment(Project project) {
        use (PropertyCategory) {
            AndroidProjectConfigurationRetriever confRetriever = new AndroidProjectConfigurationRetriever()
            AndroidManifestHelper manifestHelper = new AndroidManifestHelper()
            manifestHelper.readVersion(project.rootDir, project.getProjectConfiguration())
            AndroidBuildXmlHelper buildXmlHelper = new AndroidBuildXmlHelper()
            Properties props = new Properties()
            props.load(new FileInputStream(new File(project.rootDir,"gradle.properties")))
            props.keys().each { key->
                project[key]=props.getProperty(key)
            }
            project[ProjectConfigurationPlugin.PROJECT_NAME_PROPERTY] = buildXmlHelper.readProjectName(project.rootDir)
            project.retrieveBasicProjectData()
            AndroidSingleVariantBuilder builder = new AndroidSingleVariantBuilder(project,confRetriever.getAndroidProjectConfiguration(project))
            builder.updateAndroidConfigurationWithVariants()
            confRetriever.readAndroidProjectConfiguration(project)
            return builder
        }
    }

    public void testArtifactBuilderInfoVariantedDebug() throws Exception {
        Project project = getProject()
        AndroidSingleVariantBuilder builder = prepareEnvironment(project)
        AndroidArtifactBuilderInfo ai = builder.buildApkArtifactBuilderInfo(project, "test", null)
        assertEquals('test', ai.variant)
        assertEquals('Debug', ai.debugRelease)
        assertEquals(new File("testProjects/android/bin").absolutePath, ai.buildDirectory.absolutePath)
        assertEquals(new File("testProjects/android/bin/TestAndroidProject-debug.apk").absolutePath, ai.originalFile.absolutePath)
        assertEquals('TestAndroidProject-debug-test-1.0.1_42', ai.fullReleaseName)
        assertEquals('AdadalkjsaTest/1.0.1_42', ai.folderPrefix)
        assertEquals('TestAndroidProject-debug-test-1.0.1_42', ai.filePrefix)
    }

    public void testArtifactBuilderInfoVariantedRelease() throws Exception {
        Project project = getProject()
        AndroidSingleVariantBuilder builder = prepareEnvironment(project)
        AndroidArtifactBuilderInfo ai = builder.buildApkArtifactBuilderInfo(project, "market", null)
        assertEquals('market', ai.variant)
        assertEquals('Release', ai.debugRelease)
        assertEquals(new File("testProjects/android/bin").absolutePath, ai.buildDirectory.absolutePath)
        assertEquals(new File("testProjects/android/bin/TestAndroidProject-release.apk").absolutePath, ai.originalFile.absolutePath)
        assertEquals('TestAndroidProject-release-market-1.0.1_42', ai.fullReleaseName)
        assertEquals('AdadalkjsaTest/1.0.1_42', ai.folderPrefix)
        assertEquals('TestAndroidProject-release-market-1.0.1_42', ai.filePrefix)
    }

    public void testArtifactBuilderInfoNotVariantedDebug() throws Exception {
        Project project = getProject(false)
        AndroidSingleVariantBuilder builder = prepareEnvironment(project)
        AndroidArtifactBuilderInfo ai = builder.buildApkArtifactBuilderInfo(project, null, "Debug")
        assertEquals(null, ai.variant)
        assertEquals('Debug', ai.debugRelease)
        assertEquals(new File("testProjects/android-novariants/bin").absolutePath, ai.buildDirectory.absolutePath)
        assertEquals(new File("testProjects/android-novariants/bin/TestAndroidProject-debug.apk").absolutePath, ai.originalFile.absolutePath)
        assertEquals('TestAndroidProject-debug-1.0.1_42', ai.fullReleaseName)
        assertEquals('asdlakjljsdTest/1.0.1_42', ai.folderPrefix)
        assertEquals('TestAndroidProject-debug-1.0.1_42', ai.filePrefix)
    }

    public void testArtifactBuilderInfoNotVariantedRelease() throws Exception {
        Project project = getProject(false)
        AndroidSingleVariantBuilder builder = prepareEnvironment(project)
        AndroidArtifactBuilderInfo ai = builder.buildApkArtifactBuilderInfo(project, null, "Release")
        assertEquals(null, ai.variant)
        assertEquals('Release', ai.debugRelease)
        assertEquals(new File("testProjects/android-novariants/bin").absolutePath, ai.buildDirectory.absolutePath)
        assertEquals(new File("testProjects/android-novariants/bin/TestAndroidProject-release.apk").absolutePath, ai.originalFile.absolutePath)
        assertEquals('TestAndroidProject-release-1.0.1_42', ai.fullReleaseName)
        assertEquals('asdlakjljsdTest/1.0.1_42', ai.folderPrefix)
        assertEquals('TestAndroidProject-release-1.0.1_42', ai.filePrefix)
    }
}
