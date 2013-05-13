package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.variants.AndroidVariantsConfiguration
import com.google.common.base.Preconditions
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD

class CopySourcesTask extends DefaultTask {

    static String NAME = 'copySources'
    String description = 'Copies all sources to tmp directory for build'
    String group = AMEBA_BUILD

    @Inject
    AndroidConfiguration androidConfiguration
    @Inject
    private AndroidVariantsConfiguration androidVariantsConfiguration

    @TaskAction
    void copySources() {
        Preconditions.checkNotNull(androidVariantsConfiguration.variants)

        androidVariantsConfiguration.variants.each { variant ->
            project.ant.sync(toDir: variant.tmpDir, overwrite: true, failonerror: false, verbose: false) {
                fileset(dir: "${project.rootDir}/") {
                    exclude(name: variant.tmpDir.absolutePath + '/**/*')
                    androidConfiguration.sourceExcludes.each { exclude(name: it) }
                }
            }
        }
    }
}
