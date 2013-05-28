package com.apphance.ameba

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import static java.io.File.createTempFile

class TestUtils {

    public <T extends DefaultTask> T create(Class<T> type, Project project = null) {
        if (!project) project = ProjectBuilder.builder().build()
        String name = type.hasProperty('NAME') ? type.NAME : type.toString()
        project.task(name, type: type) as T
    }

    File createTempFile() {
        createTempFile('prefix', 'soffix')
    }
}
