package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.executor.AndroidExecutor
import com.google.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD

class RunUpdateProjectTask extends DefaultTask {

    static String NAME = 'updateProject'
    static final String PROJECT_PROPERTIES_KEY = 'project.properties'
    String description = 'Updates project using android command line tool'
    String group = AMEBA_BUILD

    @Inject
    private AndroidExecutor androidExecutor

    @TaskAction
    void runUpdate() {
        runUpdateRecursively(project.rootDir)
    }

    private void runUpdateRecursively(File currentDir) {
        runUpdateProject(project.rootDir)
        Properties prop = new Properties()
        File propFile = new File(currentDir, PROJECT_PROPERTIES_KEY)
        if (propFile.exists()) {
            prop.load(new FileInputStream(propFile))
            prop.each { key, value ->
                if (key.startsWith('android.library.reference.')) {
                    File libraryProject = new File(currentDir, value.toString())
                    runUpdateRecursively(libraryProject)
                }
            }
        }
    }

    private void runUpdateProject(File directory) {
        if (!directory.exists()) {
            throw new GradleException("The directory ${directory} to execute the command, does not exist! Your configuration is wrong.")
        }
        androidExecutor.updateProject(directory)
    }
}
