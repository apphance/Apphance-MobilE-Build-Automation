package com.apphance.flow.plugins.project.tasks

import com.apphance.flow.configuration.ProjectConfiguration
import com.apphance.flow.configuration.variants.VariantsConfiguration
import com.apphance.flow.executor.command.Command
import com.apphance.flow.executor.command.CommandExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD
import static com.apphance.flow.plugins.project.ProjectPlugin.COPY_SOURCES_TASK_NAME

class CopySourcesTask extends DefaultTask {

    static final NAME = COPY_SOURCES_TASK_NAME
    String group = FLOW_BUILD
    String description = 'Copies all sources to temporary directories, where the variants will be built.'

    @Inject ProjectConfiguration conf
    @Inject VariantsConfiguration variantsConf
    @Inject CommandExecutor executor

    @TaskAction
    void copySources() {
        conf.tmpDir.mkdirs()
        def excludes = conf.sourceExcludes.collectMany { ['--exclude', it] }
        variantsConf.variants.each { v ->
            v.tmpDir.deleteDir()
            logger.lifecycle("Copying sources from : $conf.rootDir.absolutePath to $v.tmpDir.absolutePath")
            executor.executeCommand(new Command(
                    runDir: conf.rootDir,
                    cmd: ['rsync', '-rvalp', '--executability', '.'] + excludes + [v.tmpDir.absolutePath]
            ))
        }
    }
}
