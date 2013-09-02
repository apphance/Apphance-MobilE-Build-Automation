package com.apphance.flow

import com.apphance.flow.util.FlowUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class TestUtils extends FlowUtils {

    public <T extends DefaultTask> T create(Class<T> type, Project project = null) {
        if (!project) project = ProjectBuilder.builder().build()
        String name = type.hasProperty('NAME') ? type.NAME : type.toString()
        project.task(name, type: type) as T
    }

    boolean contains(File file, String content) {
        file.readLines()*.trim().contains(content)
    }

    File newFile(File root = temporaryDir, String name, String content) {
        assert root.exists()
        File file = new File(root, name)
        file.createNewFile()
        file.text = content
        file
    }

    File newDir(File root, String name) {
        assert root.exists()
        File dir = new File(root, name)
        assert dir.mkdirs()
        dir
    }

    String removeWhitespace(String input) {
        input.replaceAll(/\s/, '')
    }
}
