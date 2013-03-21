package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.plugins.projectconfiguration.ProjectConfiguration
import com.apphance.ameba.executor.AntExecutor
import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration
import org.gradle.api.Project

import static com.apphance.ameba.PropertyCategory.getProjectConfiguration
import static com.apphance.ameba.plugins.release.ProjectReleaseCategory.getProjectReleaseConfiguration

class InstallTask {

    private AntExecutor antExecutor
    private ProjectConfiguration conf
    private ProjectReleaseConfiguration releaseConf

    InstallTask(Project project, AntExecutor antExecutor) {
        this.antExecutor = antExecutor
        this.conf = getProjectConfiguration(project)
        this.releaseConf = getProjectReleaseConfiguration(project)
    }

    void install(String variant, String debugRelease) {
        def firstLetterLowerCase = debugRelease[0].toLowerCase()
        File apkFile = new File(releaseConf.targetDirectory, "${conf.projectName}-${debugRelease}-${variant}-${conf.fullVersionString}.apk".toString())
        antExecutor.executeTarget "install${firstLetterLowerCase}", ['out.final.file': apkFile.canonicalPath]
    }
}
