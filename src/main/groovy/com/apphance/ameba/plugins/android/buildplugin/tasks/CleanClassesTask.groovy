package com.apphance.ameba.plugins.android.buildplugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD

class CleanClassesTask extends DefaultTask {

    static String NAME = 'cleanClasses'
    String description = 'Cleans only the compiled classes'
    String group = AMEBA_BUILD

    @TaskAction
    void cleanClasses() {
        project.ant.delete(dir: project.file('build'))
    }
}
