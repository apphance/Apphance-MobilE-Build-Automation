package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.executor.AndroidExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD

class UpdateProjectTask extends DefaultTask {

    static String NAME = 'updateProject'
    String description = 'Updates project using android command line tool'
    String group = AMEBA_BUILD

    @Inject AndroidConfiguration conf
    @Inject AndroidExecutor androidExecutor

    @TaskAction
    void runUpdate() {
        runUpdateRecursively(conf.rootDir)
    }

    void runUpdateRecursively(File currentDir) {
        runUpdateProject(currentDir)

        def propFile = new File(currentDir, 'project.properties')

        if (propFile.exists()) {
            def prop = new Properties()
            prop.load(new FileInputStream(propFile))
            prop.each { key, value ->
                if (key.startsWith('android.library.reference.')) {
                    runUpdateRecursively(new File(currentDir, value.toString()))
                }
            }
        }
    }

    private void runUpdateProject(File directory) {
        if (!directory.exists()) {
            throw new GradleException("The directory ${directory} to execute the command, does not exist! Your configuration is wrong.")
        }
        androidExecutor.updateProject(directory, conf.target.value, conf.projectName.value)
    }
}
