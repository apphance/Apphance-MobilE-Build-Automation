package com.apphance.ameba.android.plugins.analysis.tasks

import com.apphance.ameba.android.AndroidProjectConfiguration
import com.apphance.ameba.util.Preconditions
import org.gradle.api.GradleException
import org.gradle.api.Project

import static com.apphance.ameba.android.AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration
import static com.apphance.ameba.android.plugins.analysis.AndroidAnalysisPlugin.FINDBUGS_DEFAULT_HOME
import static com.apphance.ameba.android.plugins.analysis.AndroidAnalysisPlugin.FINDBUGS_HOME_DIR_PROPERTY

@Mixin(AndroidAnalysisMixin)
@Mixin(Preconditions)
class FindbugsTask {

    private Project project
    private AndroidProjectConfiguration androidConf

    FindbugsTask(Project project) {
        this.project = project
        this.androidConf = getAndroidProjectConfiguration(project)
    }

    public void runFindbugs() {
        URL findbugsXml = getResourceUrl(project, 'findbugs-exclude.xml')
        File analysisDir = project.file('build/analysis')
        File findbugsFile = new File(analysisDir, "findbugs-exclude.xml")

        findbugsFile.parentFile.mkdirs()
        findbugsFile.delete()
        findbugsFile << findbugsXml.getContent()

        File findbugsHomeDir = findbugsHomeDir()
        validate(findbugsHomeDir.exists() && findbugsHomeDir.isDirectory(), {
            throw new GradleException("The file ${findbugsHomeDir} should point to findbugs home directory. " +
                    "You can change it by specifying -Pfindbugs.home.dir=<DIR>")
        })
        def cp = project.configurations.findbugsConf.asPath
        def binClasses = project.file('bin/classes')
        project.ant {
            taskdef(name: 'findbugs', classname: 'edu.umd.cs.findbugs.anttask.FindBugsTask',
                    classpath: cp)
            findbugs(home: findbugsHomeDir.absolutePath,
                    output: 'xml', outputFile: 'build/analysis/findbugs-result.xml',
                    excludefilter: 'build/analysis/findbugs-exclude.xml') {
                sourcePath(path: 'src')
                "class"(location: binClasses)
                auxclassPath(path: androidConf.allJarsAsPath)
            }
        }
    }

    private File findbugsHomeDir() {
        project.hasProperty(FINDBUGS_HOME_DIR_PROPERTY) ?
            new File(project[FINDBUGS_HOME_DIR_PROPERTY].toString())
        :
            new File(FINDBUGS_DEFAULT_HOME)
    }
}
