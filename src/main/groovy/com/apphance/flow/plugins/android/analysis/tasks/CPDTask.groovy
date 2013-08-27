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

    @OutputFile File report = new File(project.rootDir, 'build/reports/cpd/cpd-result.xml')

    CPDTask(Instantiator instantiator, IsolatedAntBuilder antBuilder) {
        super(instantiator, antBuilder)
    }

    @TaskAction
    @Override
    public void run() {
        logger.lifecycle "Running CPD task in directory: ${project.rootDir.absolutePath}"

        report.parentFile.mkdirs()
        logger.info "Report dir $report.parentFile.absolutePath exists: ${report.parentFile.exists()}"
        def cp = project.configurations.pmd.asPath
        def rootDir = project.rootDir

        project.ant {
            taskdef(name: 'cpd', classname: 'net.sourceforge.pmd.cpd.CPDTask', classpath: cp)
            cpd(minimumTokenCount: '100', format: 'xml', encoding: 'UTF-8', outputFile: report.path) {
                fileset(dir: rootDir.absolutePath + '/src', includes: '**/*.java')
            }
        }
    }
}
