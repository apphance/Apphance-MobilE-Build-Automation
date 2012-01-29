package com.apphance.ameba.vcs.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper

abstract class VCSPlugin implements Plugin<Project> {
    ProjectHelper projectHelper
    ProjectConfiguration conf
    def void apply (Project project) {
        projectHelper = new ProjectHelper();
        conf = projectHelper.getProjectConfiguration(project)
        cleanVCSTask(project)
        saveReleaseInfoInVCSTask(project)
        getVCSExcludes(project).each { conf.sourceExcludes << it }
    }

    abstract void cleanVCSTask(Project project)
    abstract void saveReleaseInfoInVCSTask(Project project)
    abstract String[] getVCSExcludes(Project project)
}