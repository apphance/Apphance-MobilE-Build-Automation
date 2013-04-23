package com.apphance.ameba.plugins.android.analysis.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_ANALYSIS

class CPDTask extends DefaultTask {

    static String NAME = 'cpd'
    String group = AMEBA_ANALYSIS
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
