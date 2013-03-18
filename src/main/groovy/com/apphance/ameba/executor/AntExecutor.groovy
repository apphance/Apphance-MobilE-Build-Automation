package com.apphance.ameba.executor

import org.apache.tools.ant.Project
import org.apache.tools.ant.ProjectHelper

/**
 * Executor of ant targets.
 */
class AntExecutor {

    public static String DEBUG = "debug"
    public static String CLEAN = "clean"

    Project antProject

    AntExecutor(File rootDir, String buildFileName = 'build.xml') {
        antProject = new Project()
        antProject.setName("Ant project from $rootDir")
        antProject.initProperties()

        String buildFilePath = rootDir.absolutePath + '/' + buildFileName
        File buildFile = new File(buildFilePath)
        if (!buildFile.exists()) throw new IllegalArgumentException("No $buildFileName in $rootDir")
        ProjectHelper.configureProject(antProject, buildFile)
    }

    def executeTarget(String target, Map<String, String> properties = [:]) {
        properties.each { name, value -> antProject.setProperty(name, value) }
        antProject.executeTarget(target)
    }
}
