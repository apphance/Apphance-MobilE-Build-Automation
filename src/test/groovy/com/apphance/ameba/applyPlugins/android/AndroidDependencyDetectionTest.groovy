package com.apphance.ameba.applyPlugins.android;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.junit.Test

import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.android.AndroidProjectConfiguration
import com.apphance.ameba.android.AndroidProjectConfigurationRetriever
import com.apphance.ameba.android.plugins.AndroidPlugin


class AndroidDependencyDetectionTest extends BaseAndroidTaskTest {

    protected Project getProject() {
        Project project = super.getProject()
        project.project.plugins.apply(AndroidPlugin.class)
        return project
    }

    @Test
    public void testProjectConfiguration() {
        Project project = getProject();
        AndroidProjectConfigurationRetriever confRetriever = new AndroidProjectConfigurationRetriever()
        AndroidProjectConfiguration androidConf = confRetriever.getAndroidProjectConfiguration(project)
        if (androidConf.linkedLibraryJars.empty) {
            ProjectHelper projectHelper = new ProjectHelper()
            projectHelper.executeCommand(project, new File("testProjects/android"), ['ant', 'debug'])
            project = getProject()
            androidConf = confRetriever.getAndroidProjectConfiguration(project)
        }
        assertNotNull(androidConf.sdkDirectory )
        assertEquals([
            'FlurryAgent.jar',
            'development-apphance.jar'
        ], androidConf.libraryJars.collect { it.name} )
        assertEquals([
            'subproject',
            'subsubproject'
        ], androidConf.linkedLibraryJars.collect { it.parentFile.parentFile.name} )
    }
}
