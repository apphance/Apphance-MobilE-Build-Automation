package com.apphance.ameba.plugins.android.analysis.tasks

import com.google.inject.Inject
import org.gradle.api.Project

@Mixin(AndroidAnalysisMixin)
class PMDTask {

    @Inject Project project

    public void runPMD() {

        URL pmdXml = getResourceUrl(project, 'pmd-rules.xml')
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
