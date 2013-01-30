package com.apphance.ameba.applyPlugins.android

import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.android.AndroidProjectConfiguration
import com.apphance.ameba.android.AndroidProjectConfigurationRetriever
import com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin
import org.gradle.api.Project
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull;


class AndroidDependencyDetectionTest extends BaseAndroidTaskTest {

    @Override
    protected Project getProject() {
        Project project = super.getProject()
        project.project.plugins.apply(AndroidPlugin.class)
        return project
    }

    @Test
    public void testProjectConfiguration() {
        Project project = getProject()
        AndroidProjectConfiguration androidConf = AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration(project)
        if (androidConf.linkedLibraryJars.empty) {
            ProjectHelper projectHelper = new ProjectHelper()
            projectHelper.executeCommand(project, new File("testProjects/android/android-basic"), ['ant', 'debug'])
            project = getProject()
            androidConf = AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration(project)
        }
        assertNotNull(androidConf.sdkDirectory)
        assertEquals([
                'FlurryAgent.jar',
                'development-apphance.jar',
        ], androidConf.libraryJars.collect { it.name })
        assertEquals([
                'subproject',
                'subsubproject'
        ], androidConf.linkedLibraryJars.collect { it.parentFile.parentFile.name })
    }
}
