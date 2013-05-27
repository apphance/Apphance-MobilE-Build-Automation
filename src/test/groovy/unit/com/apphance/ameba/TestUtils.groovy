package com.apphance.ameba

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class TestUtils {

    public <T extends DefaultTask> T create(Class<T> type, Project project = null) {
        if (!project) project = ProjectBuilder.builder().build()
        String name = type.NAME
        project.task(name, type: type) as T
    }
}
