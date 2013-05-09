package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.ios.IOSVariantsConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD

class CopySourcesTask extends DefaultTask {

    static final NAME = 'copySources'
    String group = AMEBA_BUILD
    String description = 'Copies all sources to tmp directories for build'

    @Inject
    IOSConfiguration conf
    @Inject
    IOSVariantsConfiguration variantsConf

    @TaskAction
    void copySources() {
        variantsConf.variants.each { v ->
            ant.sync(toDir: v.tmpDir, failonerror: false, overwrite: true, verbose: false) {
                fileset(dir: "${conf.rootDir}/") {
                    exclude(name: v.tmpDir.absolutePath + '/**/*')
                    conf.sourceExcludes.each { e ->
                        exclude(name: e)
                    }
                }
            }
        }
    }
}
