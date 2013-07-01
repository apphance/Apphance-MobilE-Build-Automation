package com.apphance.flow.plugins.android.buildplugin.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD
import static com.apphance.flow.plugins.project.ProjectPlugin.COPY_SOURCES_TASK_NAME
import static com.apphance.flow.util.file.FileManager.relativeTo

class CopySourcesTask extends DefaultTask {

    static final NAME = COPY_SOURCES_TASK_NAME
    String group = FLOW_BUILD
    String description = 'Copies all sources to flow-tmp directory for build'

    @Inject AndroidConfiguration conf
    @Inject AndroidReleaseConfiguration releaseConf
    @Inject AndroidVariantsConfiguration variantsConf

    @TaskAction
    //http://ant.apache.org/manual/dirtasks.html#defaultexcludes
    void copySources() {
        def absoluteRoot = conf.rootDir.absolutePath
        variantsConf.variants.each { v ->
            v.tmpDir.deleteDir()
            logger.lifecycle("Copying sources from : ${conf.rootDir.absolutePath} to $v.tmpDir")
            ant.sync(toDir: v.tmpDir, overwrite: true, failonerror: true, verbose: logger.isDebugEnabled()) {
                fileset(dir: "${conf.rootDir.absolutePath}/") {
                    exclude(name: relativeTo(absoluteRoot, conf.tmpDir.absolutePath).name + '/**/*')
                    exclude(name: relativeTo(absoluteRoot, variantsConf.variantsDir.absolutePath).name + '/**/*')
                    exclude(name: relativeTo(absoluteRoot, releaseConf.otaDir.absolutePath).name + '/**/*')
                    exclude(name: relativeTo(absoluteRoot, conf.logDir.absolutePath).name + '/**/*')
                    exclude(name: 'log/**/*')
                    conf.sourceExcludes.each { exclude(name: it) }
                }
            }
        }
    }
}
