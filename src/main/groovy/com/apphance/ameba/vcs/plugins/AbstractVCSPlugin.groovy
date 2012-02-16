package com.apphance.ameba.vcs.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.vcs.plugins.git.GitPlugin
import com.apphance.ameba.vcs.plugins.mercurial.MercurialPlugin

abstract class AbstractVCSPlugin implements Plugin<Project> {
    ProjectConfiguration conf

    def void apply (Project project) {
        ProjectHelper.checkExactlyOnePluginIsLoaded(project, this.class, MercurialPlugin.class, GitPlugin.class)
        use (PropertyCategory) {
            conf = project.getProjectConfiguration()
            cleanVCSTask(project)
            saveReleaseInfoInVCSTask(project)
            getVCSExcludes(project).each { conf.sourceExcludes << it }
        }
    }

    abstract void cleanVCSTask(Project project)
    abstract void saveReleaseInfoInVCSTask(Project project)
    abstract String[] getVCSExcludes(Project project)
}