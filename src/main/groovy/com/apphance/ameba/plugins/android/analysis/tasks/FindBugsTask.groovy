package com.apphance.ameba.plugins.android.analysis.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.util.Preconditions
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_ANALYSIS

@Mixin(Preconditions)
class FindBugsTask extends DefaultTask {

    public static final String FINDBUGS_HOME_DIR_PROPERTY = 'findbugs.home.dir'
    public static final String FINDBUGS_DEFAULT_HOME = '/var/lib/analysis/findbugs'

    static String NAME = 'findbugs'
    String group = AMEBA_ANALYSIS
    String description = 'Runs Findbugs analysis on project'

    @Inject
    AndroidConfiguration androidConfiguration
    @Inject
    AndroidAnalysisResourceLocator resourceLocator

    @TaskAction
    public void runFindbugs() {
        URL findbugsXml = resourceLocator.getResourceUrl(project, 'findbugs-exclude.xml')
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
                auxclassPath(path: androidConfiguration.allJarsAsPath)
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
