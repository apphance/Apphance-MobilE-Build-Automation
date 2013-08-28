package com.apphance.flow.plugins.android.analysis.tasks

import org.gradle.api.Project
import org.gradle.api.internal.project.IsolatedAntBuilder
import org.gradle.api.plugins.quality.Pmd
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.reflect.Instantiator

class CPDTask extends Pmd {

    static String NAME = 'cpd'
    String group = 'verification'
    String description = 'Runs CPD (duplicated code) analysis on project'
    def runner = new CPDRunner()

    @OutputFile File report = new File(project.rootDir, 'build/reports/cpd/cpd-result.xml')

    CPDTask(Instantiator instantiator, IsolatedAntBuilder antBuilder) {
        super(instantiator, antBuilder)
    }

    @TaskAction
    @Override
    public void run() {
        logger.lifecycle "Running CPD task in directory: ${project.rootDir.absolutePath}"
        report.parentFile.mkdirs()
        runner.runAnt(project, report)
    }

    class CPDRunner {
        void runAnt(Project project, File report) {
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
}
