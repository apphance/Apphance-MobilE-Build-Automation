package com.apphance.ameba.android.plugins.analysis.tasks

import com.apphance.ameba.android.AndroidProjectConfiguration
import org.gradle.api.Project

import static com.apphance.ameba.android.AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration

@Mixin(AndroidAnalysisMixin)
class CheckStyleTask {

    private Project project
    private AndroidProjectConfiguration androidConf

    CheckStyleTask(Project project) {
        this.project = project
        this.androidConf = getAndroidProjectConfiguration(project)
    }

    public void runCheckStyle() {

        URL checkstyleXml = getResourceUrl(project, 'checkstyle.xml')
        File analysisDir = project.file('build/analysis')
        File checkstyleFile = new File(analysisDir, 'checkstyle.xml')

        checkstyleFile.parentFile.mkdirs()
        checkstyleFile.delete()
        checkstyleFile << checkstyleXml.getContent()

        URL checkstyleSuppressionXml = getResourceUrl(project, 'checkstyle-suppressions.xml')
        File checkstyleSuppressionFile = new File(analysisDir, 'checkstyle-suppressions.xml')

        checkstyleSuppressionFile.parentFile.mkdirs()
        checkstyleSuppressionFile.delete()
        checkstyleSuppressionFile << checkstyleSuppressionXml.getContent()

        URL checkstyleLocalSuppressionXml = getResourceUrl(project, 'checkstyle-local-suppressions.xml')
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
                classpath(path: androidConf.allJarsAsPath)
                fileset(dir: 'src', includes: '**/*.java')
            }
        }
    }
}
