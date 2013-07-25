package com.apphance.flow

import com.google.common.io.Files
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class TestUtils {

    public <T extends DefaultTask> T create(Class<T> type, Project project = null) {
        if (!project) project = ProjectBuilder.builder().build()
        String name = type.hasProperty('NAME') ? type.NAME : type.toString()
        project.task(name, type: type) as T
    }

    File getTemporaryDir() {
        File file = Files.createTempDir()
        file.deleteOnExit()
        file
    }

    boolean contains(File file, String content) {
        file.readLines()*.trim().contains(content)
    }
}
