package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantsConfiguration
import com.google.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD

class CopySourcesTask extends DefaultTask {

    static String NAME = 'copySources'
    String description = 'Copies all sources to tmp directory for build'
    String group = AMEBA_BUILD

    @Inject AndroidConfiguration androidConfiguration
    @Inject AndroidVariantsConfiguration androidVariantsConfiguration

    @TaskAction
    void copySources() {
        assert androidVariantsConfiguration.variants != null

        androidVariantsConfiguration.variants.each { variant ->
            project.ant.sync(toDir: variant.tmpDirectory, overwrite: true, failonerror: false, verbose: false) {
                fileset(dir: "${project.rootDir}/") {
                    exclude(name: variant.tmpDirectory.absolutePath + '/**/*')
                    androidConfiguration.sourceExcludes.each { exclude(name: it) }
                }
            }
        }
    }
}
