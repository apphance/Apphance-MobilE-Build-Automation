package com.apphance.ameba.plugins.android.analysis.tasks

import org.gradle.api.Project

class CPDTask {

    private Project project

    CPDTask(Project project) {
        this.project = project
    }

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
