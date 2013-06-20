package com.apphance.flow

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

    File getTempFile() {
        File file = createTempFile('prefix', 'suffix')
        file.deleteOnExit()
        file
    }

    boolean contains(File file, String content) {
        file.readLines()*.trim().contains(content)
    }
}
