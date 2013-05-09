package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantConfiguration
import com.apphance.ameba.executor.AntExecutor
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD

class InstallTask extends DefaultTask {

    String group = AMEBA_BUILD

    @Inject
    private AndroidConfiguration androidConf
    @Inject
    private AndroidReleaseConfiguration androidReleaseConf

    @Inject
    AntExecutor antExecutor

    AndroidVariantConfiguration variant

    @TaskAction
    void install() {
        String debugRelease = variant.mode.capitalize()
        def firstLetterLowerCase = debugRelease[0].toLowerCase()
        File targetDirectory = androidReleaseConf.targetDirectory
        def apkName = "${androidConf.projectName.value}-${debugRelease}-${variant}-${androidConf.versionString}.apk".toString()
        File apkFile = new File(targetDirectory, apkName)
        antExecutor.executeTarget project.rootDir, "install${firstLetterLowerCase}", ['out.final.file': apkFile.canonicalPath]
    }

    @Override
    String getDescription() {
        "Installs $name"
    }
}
