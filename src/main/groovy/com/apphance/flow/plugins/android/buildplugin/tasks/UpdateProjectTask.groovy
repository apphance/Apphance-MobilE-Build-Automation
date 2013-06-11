package com.apphance.flow.plugins.android.buildplugin.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.executor.AndroidExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD

class UpdateProjectTask extends DefaultTask {

    static String NAME = 'updateProject'
    String description = 'Updates project using android command line tool'
    String group = FLOW_BUILD

    @Inject AndroidConfiguration conf
    @Inject AndroidExecutor androidExecutor

    @TaskAction
    void runUpdate() {
        runRecursivelyInAllSubProjects(conf.rootDir, this.&runUpdateProject)
    }

    static void runRecursivelyInAllSubProjects(File currentDir, Closure method) {
        method(currentDir)

        def propFile = new File(currentDir, 'project.properties')

        if (propFile.exists()) {
            def prop = new Properties()
            prop.load(new FileInputStream(propFile))
            prop.each { String key, String value ->
                if (key.startsWith('android.library.reference.')) {
                    runRecursivelyInAllSubProjects(new File(currentDir, value), method)
                }
            }
        }
    }

    void runUpdateProject(File directory) {
        if (!directory.exists()) {
            throw new GradleException("The directory $directory to execute the command, does not exist! Your configuration is wrong.")
        }
        androidExecutor.updateProject(directory, conf.target.value, conf.projectName.value)
    }
}
