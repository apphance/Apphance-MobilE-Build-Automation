package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.configuration.ios.IOSConfiguration
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

    @TaskAction
    void copySources() {
        conf.allTargets.each { target ->
            conf.allConfigurations.each { configuration ->
                if (!conf.isBuildExcluded(target + "-" + configuration)) {
                    ant.sync(toDir: tmpDir(target, configuration),
                            failonerror: false, overwrite: true, verbose: false) {
                        fileset(dir: "${project.rootDir}/") {
                            exclude(name: tmpDir(target, configuration).absolutePath + '/**/*')
                            conf.sourceExcludes.each { exclude(name: it) }
                        }
                    }
                }
            }
        }
    }

    private File tmpDir(String target, String configuration) {
        new File(conf.tmpDir, "${conf.projectName.value}-$target-$configuration")
    }
}
