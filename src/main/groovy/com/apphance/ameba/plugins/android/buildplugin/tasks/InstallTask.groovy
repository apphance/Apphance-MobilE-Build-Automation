package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantConfiguration
import com.apphance.ameba.executor.AntExecutor
import com.google.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD

class InstallTask extends DefaultTask {

    String group = AMEBA_BUILD

    @Inject
    private AndroidConfiguration androidConf
    @Inject
    private AndroidReleaseConfiguration androidReleaseConf

    AndroidVariantConfiguration variant

    @TaskAction
    void install() {
        String debugRelease = variant.mode.value.toLowerCase().capitalize()
        def firstLetterLowerCase = debugRelease[0].toLowerCase()
        File targetDirectory = androidReleaseConf.targetDirectory
        def apkName = "${androidConf.projectName.value}-${debugRelease}-${variant}-${androidConf.versionString.value}.apk".toString()
        File apkFile = new File(targetDirectory, apkName)
        new AntExecutor(project.rootDir).executeTarget "install${firstLetterLowerCase}", ['out.final.file': apkFile.canonicalPath]
    }

    @Override
    String getDescription() {
        "Installs $name"
    }
}
