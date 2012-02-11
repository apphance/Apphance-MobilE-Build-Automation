package com.apphance.ameba.vcs.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.PropertyCategory

abstract class VCSPlugin implements Plugin<Project> {
    ProjectConfiguration conf
    def void apply (Project project) {
        use (PropertyCategory) {
            conf = project.getProjectConfiguration()
            cleanVCSTask(project)
            saveReleaseInfoInVCSTask(project)
            getVCSExcludes(project).each { conf.sourceExcludes << it }
            prepareShowPropertiesTask(project)
        }
    }

    abstract void cleanVCSTask(Project project)
    abstract void saveReleaseInfoInVCSTask(Project project)
    abstract String[] getVCSExcludes(Project project)
    abstract void prepareShowPropertiesTask(Project project)
}