package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.configuration.ios.IOSConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD

class CopyDebugSourcesTask extends DefaultTask {

    static final NAME = 'copyDebugSources'
    String description = 'Copies all debug sources to tmp directories for build'
    String group = AMEBA_BUILD
    @Inject
    IOSConfiguration conf

    @TaskAction
    void copyDebugSources() {
        String debugConfiguration = 'Debug'
        conf.allTargets.each { target ->
            ant.sync(toDir: tmpDir(target, debugConfiguration),
                    failonerror: false, overwrite: true, verbose: false) {
                fileset(dir: "${project.rootDir}/") {
                    exclude(name: tmpDir(target, debugConfiguration).absolutePath + '/**/*')
                    conf.sourceExcludes.each { exclude(name: it) }
                }
            }
        }
    }

    private File tmpDir(String target, String configuration) {
        new File(conf.tmpDir, "${conf.projectName.value}-$target-$configuration")
    }
}
