package com.apphance.ameba.applyPlugins.wp7

import static org.junit.Assert.*

import org.codehaus.groovy.runtime.ProcessGroovyMethods
import org.gradle.api.Project;
import org.junit.Test

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.android.AndroidManifestHelper
import com.apphance.ameba.wp7.plugins.test.Wp7TestPlugin;

class Wp7TestPluginTasksTest extends AbstractBaseWp7TasksTest {

    File testProject = new File("testProjects/wp7")
    File templateFile = new File("templates/wp7")

    protected Project getProject() {
        Project project = super.getProject()
        project.project.plugins.apply(Wp7TestPlugin.class)
        return project
    }

    @Test
    public void testTestTaskAvailable() {

    }

}
