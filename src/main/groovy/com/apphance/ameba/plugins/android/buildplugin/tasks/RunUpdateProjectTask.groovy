package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.executor.AndroidExecutor
import com.apphance.ameba.executor.command.CommandExecutor
import org.gradle.api.GradleException

import static com.apphance.ameba.plugins.android.buildplugin.AndroidPlugin.PROJECT_PROPERTIES_KEY

class RunUpdateProjectTask {

    private CommandExecutor executor

    private AndroidExecutor androidExecutor

    RunUpdateProjectTask(CommandExecutor executor, AndroidExecutor androidExecutor) {
        this.executor = executor
        this.androidExecutor = androidExecutor
    }

    void runUpdateRecursively(File currentDir, boolean reRun) {
        runUpdateProject(currentDir, reRun)
        Properties prop = new Properties()
        File propFile = new File(currentDir, PROJECT_PROPERTIES_KEY)
        if (propFile.exists()) {
            prop.load(new FileInputStream(propFile))
            prop.each { key, value ->
                if (key.startsWith('android.library.reference.')) {
                    File libraryProject = new File(currentDir, value.toString())
                    runUpdateRecursively(libraryProject, reRun)
                }
            }
        }
    }

    private void runUpdateProject(File directory, boolean reRun) {
        if (!new File(directory, 'local.properties').exists() || reRun) {
            if (!directory.exists()) {
                throw new GradleException("The directory ${directory} to execute the command, does not exist! Your configuration is wrong.")
            }
            androidExecutor.updateProject(directory)
        }
    }
}
