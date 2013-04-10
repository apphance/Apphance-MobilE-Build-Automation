package com.apphance.ameba.plugins.android.analysis.tasks

import com.google.inject.Inject
import org.gradle.api.Project

class CPDTask {

    @Inject Project project

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
