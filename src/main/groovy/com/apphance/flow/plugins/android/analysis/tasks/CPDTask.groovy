package com.apphance.flow.plugins.android.analysis.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_ANALYSIS

class CPDTask extends DefaultTask {

    static String NAME = 'cpd'
    String group = FLOW_ANALYSIS
    String description = 'Runs CPD (duplicated code) analysis on project'

    @TaskAction
    public void runCPD() {

        def cp = project.configurations.pmdConf.asPath
        project.ant {
            taskdef(name: 'cpd', classname: 'net.sourceforge.pmd.cpd.CPDTask', classpath: cp)
            cpd(minimumTokenCount: '100', format: 'xml', encoding: 'UTF-8',
                    outputFile: 'build/analysis/cpd-result.xml') {
                fileset(dir: 'src', includes: '**/*.java')
            }
        }
    }
}
