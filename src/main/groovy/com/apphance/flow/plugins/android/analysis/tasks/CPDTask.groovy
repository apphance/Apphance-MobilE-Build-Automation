package com.apphance.flow.plugins.android.analysis.tasks

import org.gradle.api.internal.project.IsolatedAntBuilder
import org.gradle.api.plugins.quality.Pmd
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.reflect.Instantiator

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_ANALYSIS

class CPDTask extends Pmd {

    static String NAME = 'cpd'
    String group = FLOW_ANALYSIS
    String description = 'Runs CPD (duplicated code) analysis on project'

    CPDTask(Instantiator instantiator, IsolatedAntBuilder antBuilder) {
        super(instantiator, antBuilder)
    }

    @OutputFile
    File report = new File('build/reports/cpd/cpd-result.xml')

    @TaskAction
    @Override
    public void run() {
        logger.lifecycle "Running CPD task"

        def cp = project.configurations.pmd.asPath
        report.parentFile.mkdirs()

        project.ant {
            taskdef(name: 'cpd', classname: 'net.sourceforge.pmd.cpd.CPDTask', classpath: cp)
            cpd(minimumTokenCount: '100', format: 'xml', encoding: 'UTF-8', outputFile: report.path) {
                fileset(dir: 'src', includes: '**/*.java')
            }
        }
    }
}
