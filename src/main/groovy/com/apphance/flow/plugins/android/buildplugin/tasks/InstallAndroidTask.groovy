package com.apphance.flow.plugins.android.buildplugin.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.executor.AntExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD

class InstallAndroidTask extends DefaultTask {

    String group = FLOW_BUILD

    @Inject AndroidConfiguration conf
    @Inject AndroidReleaseConfiguration releaseConf
    @Inject AntExecutor antExecutor

    AndroidVariantConfiguration variant

    @TaskAction
    void install() {
        String debugRelease = variant.mode.capitalize()
        def firstLetterLowerCase = debugRelease[0].toLowerCase()
        def apkName = "${conf.projectNameNoWhiteSpace}-${debugRelease}-${variant}-${conf.versionString}.apk".toString()
        File apkFile = new File(releaseConf.releaseDir, apkName)
        antExecutor.executeTarget conf.rootDir, "install${firstLetterLowerCase}", ['out.final.file': apkFile.canonicalPath]
    }

    @Override
    String getDescription() {
        "Installs $name"
    }
}
