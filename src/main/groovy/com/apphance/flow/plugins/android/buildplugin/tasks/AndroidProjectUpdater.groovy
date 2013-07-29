package com.apphance.flow.plugins.android.buildplugin.tasks

import com.apphance.flow.executor.AndroidExecutor
import groovy.transform.PackageScope
import org.gradle.api.GradleException
import org.gradle.api.logging.Logging

import javax.inject.Inject

class AndroidProjectUpdater {

    def logger = Logging.getLogger(this.class)

    @Inject AndroidExecutor executor

    void updateRecursively(File dir, String target = null, String name = null) {
        updateProject(dir, target, name)
        def propFile = new File(dir, 'project.properties')

        if (propFile.exists()) {
            def prop = new Properties()
            prop.load(propFile.newInputStream())
            prop.each { String key, String value ->
                if (key.startsWith('android.library.reference.')) {
                    updateRecursively(new File(dir, value))
                }
            }
        }
    }

    @PackageScope
    void updateProject(File directory, String target, String name) {
        if (!directory.exists()) {
            throw new GradleException("The directory $directory to execute the command, does not exist! Your configuration is wrong.")
        }
        logger.lifecycle "Updating project: $directory.absolutePath with target: $target, and name: $name"
        executor.updateProject directory, target, name
    }
}
