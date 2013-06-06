package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.ameba.executor.AntExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD

class InstallTask extends DefaultTask {

    String group = AMEBA_BUILD

    @Inject AndroidConfiguration conf
    @Inject AndroidReleaseConfiguration releaseConf
    @Inject AntExecutor antExecutor

    AndroidVariantConfiguration variant

    @TaskAction
    void install() {
        String debugRelease = variant.mode.capitalize()
        def firstLetterLowerCase = debugRelease[0].toLowerCase()
        def apkName = "${conf.projectName.value}-${debugRelease}-${variant}-${conf.versionString}.apk".toString()
        File apkFile = new File(releaseConf.targetDir, apkName)
        antExecutor.executeTarget conf.rootDir, "install${firstLetterLowerCase}", ['out.final.file': apkFile.canonicalPath]
    }

    @Override
    String getDescription() {
        "Installs $name"
    }
}
