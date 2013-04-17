package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.executor.AntExecutor

class InstallTaskNoVariant {

    private AntExecutor antExecutor
    private AndroidConfiguration conf
    private AndroidReleaseConfiguration releaseConf

    InstallTaskNoVariant(AntExecutor antExecutor) {
        this.antExecutor = antExecutor
    }

    void install(String debugRelease) {
        def firstLetterLowerCase = debugRelease[0].toLowerCase()
        File apkFile = new File(releaseConf.targetDirectory, "${conf.projectName}-${debugRelease}-${conf.fullVersionString}.apk")
        antExecutor.executeTarget "install${firstLetterLowerCase}", ['out.final.file': apkFile.canonicalPath]
    }
}

