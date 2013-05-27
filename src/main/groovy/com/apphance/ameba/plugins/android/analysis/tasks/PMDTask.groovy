package com.apphance.ameba.plugins.android.analysis.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_ANALYSIS

class PMDTask extends DefaultTask {

    static String NAME = 'pmd'
    String group = AMEBA_ANALYSIS
    String description = 'Runs PMD analysis on project'

    @Inject AndroidAnalysisResourceLocator resourceLocator

    @TaskAction
    public void runPMD() {

        URL pmdXml = resourceLocator.getResourceUrl(project, 'pmd-rules.xml')
        File analysisDir = project.file('build/analysis')
        File pmdFile = new File(analysisDir, 'pmd-rules.xml')

        pmdFile.parentFile.mkdirs()
        pmdFile.delete()
        pmdFile << pmdXml.getContent()

        def cp = project.configurations.pmdConf.asPath

        project.ant {
            taskdef(name: 'pmd', classname: 'net.sourceforge.pmd.ant.PMDTask',
                    classpath: cp)
            pmd(shortFilenames: 'false', failonruleviolation: 'false',
                    rulesetfiles: 'build/analysis/pmd-rules.xml') {
                formatter(type: 'xml', toFile: 'build/analysis/pmd-result.xml')
                fileset(dir: 'src') { include(name: '**/*.java') }
            }
        }
    }
}
