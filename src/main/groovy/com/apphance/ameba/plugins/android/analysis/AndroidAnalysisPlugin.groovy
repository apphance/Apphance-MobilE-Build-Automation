package com.apphance.ameba.plugins.android.analysis

import com.apphance.ameba.configuration.android.AndroidAnalysisConfiguration
import com.apphance.ameba.plugins.android.analysis.tasks.CPDTask
import com.apphance.ameba.plugins.android.analysis.tasks.CheckstyleTask
import com.apphance.ameba.plugins.android.analysis.tasks.FindBugsTask
import com.apphance.ameba.plugins.android.analysis.tasks.PMDTask
import com.google.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.Project

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_ANALYSIS
import static org.gradle.api.plugins.JavaPlugin.CLASSES_TASK_NAME

/**
 * Provides static code analysis.
 *
 */
class AndroidAnalysisPlugin implements Plugin<Project> {

    @Inject
    private AndroidAnalysisConfiguration analysisConf

    @Override
    void apply(Project project) {
        if (analysisConf.isActive()) {

            project.configurations.add('pmdConf')
            project.dependencies.add('pmdConf', 'pmd:pmd:4.3')
            project.task(PMDTask.NAME,
                    type: PMDTask)

            project.task(CPDTask.NAME,
                    type: CPDTask)

            project.configurations.add('findbugsConf')
            project.dependencies.add('findbugsConf', 'com.google.code.findbugs:findbugs:2.0.1')
            project.dependencies.add('findbugsConf', 'com.google.code.findbugs:findbugs-ant:2.0.1')
            project.task(FindBugsTask.NAME,
                    type: FindBugsTask,
                    dependsOn: CLASSES_TASK_NAME)


            project.configurations.add('checkstyleConf')
            project.dependencies.add('checkstyleConf', 'com.puppycrawl.tools:checkstyle:5.6')
            project.task(CheckstyleTask.NAME,
                    type: CheckstyleTask,
                    dependsOn: CLASSES_TASK_NAME)

            project.task('analysis',
                    description: 'Runs all analysis on project',
                    group: AMEBA_ANALYSIS,
                    dependsOn: [FindBugsTask.NAME, PMDTask.NAME, CPDTask.NAME, CheckstyleTask.NAME])
        }
    }
}
