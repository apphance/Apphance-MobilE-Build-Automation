package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.android.variants.AndroidVariantsConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD
import static com.apphance.ameba.util.file.FileManager.relativeTo

class CopySourcesTask extends DefaultTask {

    static String NAME = 'copySources'
    String description = 'Copies all sources to tmp directory for build'
    String group = AMEBA_BUILD

    @Inject
    AndroidConfiguration conf
    @Inject
    AndroidVariantsConfiguration variantsConf
    @Inject
    AndroidReleaseConfiguration releaseConf

    @TaskAction
    void copySources() {
        def absoluteRoot = conf.rootDir.absolutePath

        variantsConf.variants.each { variant ->
            variant.tmpDir.deleteDir()
            ant.sync(toDir: variant.tmpDir, overwrite: true, failonerror: true, verbose: true) {
                fileset(dir: "${conf.rootDir.absolutePath}/") {
                    exclude(name: relativeTo(absoluteRoot, conf.tmpDir.absolutePath).name + '/**/*')
                    exclude(name: relativeTo(absoluteRoot, variantsConf.variantsDir.absolutePath).name + '/**/*')
                    exclude(name: relativeTo(absoluteRoot, releaseConf.otaDir.absolutePath).name + '/**/*')
                    exclude(name: relativeTo(absoluteRoot, conf.logDir.absolutePath).name + '/**/*')
                    conf.sourceExcludes.each { exclude(name: it) }
                }
            }
        }
    }
}
