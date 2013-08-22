package com.apphance.flow.plugins.android.analysis.tasks

import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.executor.AndroidExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_ANALYSIS

public class LintTask extends DefaultTask {

    static String NAME = 'lint'
    String group = FLOW_ANALYSIS
    String description = 'Runs lint analysis on main variant of project'

    @Inject AndroidVariantsConfiguration androidVariantsConf
    @Inject AndroidExecutor androidExecutor

    @TaskAction
    public void run() {
        logger.lifecycle "Running lint task"

        def lintReport = new File('build/reports/lint/lint-raport.html')
        lintReport.parentFile.mkdirs()
        def mainVariant = androidVariantsConf.variants.find { it.name == androidVariantsConf.mainVariant }

        androidExecutor.runLint mainVariant.tmpDir, lintReport
    }
}
