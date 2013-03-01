package com.apphance.ameba

import junit.framework.TestCase
import org.gradle.api.Project
import org.gradle.api.Task

abstract class BaseTaskTest extends TestCase {

    protected void verifyTasksInGroup(Project project, Collection<String> taskNames, String group) {
        taskNames.each { taskName ->
            Task task = project.tasks[taskName]
            assertEquals("Task ${task} should be in ${group}", group, task.group)
        }
    }
}
