package com.apphance.flow.plugins.android.buildplugin.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.executor.AndroidExecutor
import groovy.transform.PackageScope
import org.gradle.api.GradleException

import javax.inject.Inject

class AndroidProjectUpdater {

    @Inject AndroidConfiguration conf
    @Inject AndroidExecutor executor

    void runRecursivelyInAllSubProjects(File dir) {
        updateProject(dir)
        def propFile = new File(dir, 'project.properties')

        if (propFile.exists()) {
            def prop = new Properties()
            prop.load(propFile.newInputStream())
            prop.each { String key, String value ->
                if (key.startsWith('android.library.reference.')) {
                    runRecursivelyInAllSubProjects(new File(dir, value))
                }
            }
        }
    }

    @PackageScope
    void updateProject(File directory) {
        if (!directory.exists()) {
            throw new GradleException("The directory $directory to execute the command, does not exist! Your configuration is wrong.")
        }
        executor.updateProject(directory, conf.target.value, conf.projectName.value)
    }
}
