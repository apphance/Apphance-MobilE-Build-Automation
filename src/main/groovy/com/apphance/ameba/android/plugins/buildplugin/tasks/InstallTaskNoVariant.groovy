package com.apphance.ameba.android.plugins.buildplugin.tasks

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.executor.AntExecutor
import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration
import org.gradle.api.Project

import static com.apphance.ameba.PropertyCategory.getProjectConfiguration
import static com.apphance.ameba.plugins.release.ProjectReleaseCategory.getProjectReleaseConfiguration

class InstallTaskNoVariant {

    private AntExecutor antExecutor
    private ProjectConfiguration conf
    private ProjectReleaseConfiguration releaseConf

    InstallTaskNoVariant(Project project, AntExecutor antExecutor) {
        this.antExecutor = antExecutor
        this.conf = getProjectConfiguration(project)
        this.releaseConf = getProjectReleaseConfiguration(project)
    }

    void install(String debugRelease) {
        def firstLetterLowerCase = debugRelease[0].toLowerCase()
        File apkFile = new File(releaseConf.targetDirectory, "${conf.projectName}-${debugRelease}-${conf.fullVersionString}.apk")
        antExecutor.executeTarget "install${firstLetterLowerCase}", ['out.final.file': apkFile.canonicalPath]
    }
}

