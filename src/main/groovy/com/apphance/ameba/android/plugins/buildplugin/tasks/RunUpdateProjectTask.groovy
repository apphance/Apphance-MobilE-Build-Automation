package com.apphance.ameba.android.plugins.buildplugin.tasks

import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import org.gradle.api.GradleException

import static com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin.PROJECT_PROPERTIES_KEY

class RunUpdateProjectTask {

    private CommandExecutor executor

    RunUpdateProjectTask(CommandExecutor executor) {
        this.executor = executor
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
            try {
                executor.executeCommand(new Command(runDir: directory, cmd: [
                        'android',
                        'update',
                        'project',
                        '-p',
                        '.',
                        '-s'
                ], failOnError: false
                ))
            } catch (IOException e) {
                throw new GradleException("""The android utility is probably not in your PATH. Please add it!
    BEWARE! For eclipse junit build it's best to add symbolic link
    to your \$ANDROID_HOME/tools/android in /usr/bin""", e)
            }
        }
    }
}
