package com.apphance.flow.plugins.android.analysis.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_ANALYSIS

class CheckstyleTask extends DefaultTask {

    static String NAME = 'checkstyle'
    String group = FLOW_ANALYSIS
    String description = 'Runs Checkstyle analysis on project'

    @Inject AndroidConfiguration androidConfiguration
    @Inject AndroidAnalysisResourceLocator resourceLocator

    @TaskAction
    public void runCheckStyle() {

        URL checkstyleXml = resourceLocator.getResourceUrl(project, 'checkstyle.xml')
        File analysisDir = project.file('build/analysis')
        File checkstyleFile = new File(analysisDir, 'checkstyle.xml')

        checkstyleFile.parentFile.mkdirs()
        checkstyleFile.delete()
        checkstyleFile << checkstyleXml.getContent()

        URL checkstyleSuppressionXml = resourceLocator.getResourceUrl(project, 'checkstyle-suppressions.xml')
        File checkstyleSuppressionFile = new File(analysisDir, 'checkstyle-suppressions.xml')

        checkstyleSuppressionFile.parentFile.mkdirs()
        checkstyleSuppressionFile.delete()
        checkstyleSuppressionFile << checkstyleSuppressionXml.getContent()

        URL checkstyleLocalSuppressionXml = resourceLocator.getResourceUrl(project, 'checkstyle-local-suppressions.xml')
        File checkstyleLocalSuppressionFile = new File(analysisDir, 'checkstyle-local-suppressions.xml')

        checkstyleLocalSuppressionFile.parentFile.mkdirs()
        checkstyleLocalSuppressionFile.delete()
        checkstyleLocalSuppressionFile << checkstyleLocalSuppressionXml.getContent()

        def cp = project.configurations.checkstyleConf.asPath

        project.ant {
            taskdef(resource: 'checkstyletask.properties', classpath: cp)
            checkstyle(config: 'build/analysis/checkstyle.xml', failOnViolation: false) {
                formatter(type: 'xml', tofile: 'build/analysis/checkstyle-report.xml')
                classpath(path: 'bin/classes')
                classpath(path: cp)
                classpath(path: androidConfiguration.allJarsAsPath)
                fileset(dir: 'src', includes: '**/*.java')
            }
        }
    }
}
