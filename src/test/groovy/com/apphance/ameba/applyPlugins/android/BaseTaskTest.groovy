package com.apphance.ameba.applyPlugins.android

import java.util.Collection

import junit.framework.TestCase

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder

import com.apphance.ameba.plugins.ProjectConfigurationPlugin

abstract class BaseTaskTest extends TestCase{
    protected Project getProject() {
        ProjectBuilder projectBuilder = ProjectBuilder.builder()
        Project project = projectBuilder.build()
        project.project.plugins.apply(ProjectConfigurationPlugin.class)
        return project
    }

    protected void verifyTasksInGroup(Project project, Collection<String> taskNames, String group) {
        taskNames.each { taskName ->
            Task task = project.tasks[taskName]
            assertEquals("Task ${task} should be in ${group}", group, task.group)
        }
        def currentGroupTasks = project.tasks.findAll { it.group == group }
        assertEquals("There are more tasks than exepected (${taskNames} vs. ${currentGroupTasks}" ,
                taskNames.size(), currentGroupTasks.size())
    }
}
