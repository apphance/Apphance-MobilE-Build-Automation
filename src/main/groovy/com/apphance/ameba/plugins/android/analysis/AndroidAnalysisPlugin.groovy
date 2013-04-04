package com.apphance.ameba.plugins.android.analysis

import com.apphance.ameba.plugins.android.analysis.tasks.CPDTask
import com.apphance.ameba.plugins.android.analysis.tasks.CheckStyleTask
import com.apphance.ameba.plugins.android.analysis.tasks.FindBugsTask
import com.apphance.ameba.plugins.android.analysis.tasks.PMDTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_ANALYSIS
import static org.gradle.api.plugins.JavaPlugin.CLASSES_TASK_NAME

/**
 * Provides static code analysis.
 *
 */
class AndroidAnalysisPlugin implements Plugin<Project> {

    public static final String PMD_TASK_NAME = 'pmd'
    public static final String CPD_TASK_NAME = 'cpd'
    public static final String FINDBUGS_TASK_NAME = 'findbugs'
    public static final String CHECKSTYLE_TASK_NAME = 'checkstyle'
    public static final String ANALYSIS_TASK_NAME = 'analysis'

    private Project project

    @Override
    void apply(Project project) {
        this.project = project
        preparePmdTask()
        prepareCpdTask()
        prepareFindbugsTask()
        prepareCheckstyleTask()
        prepareAnalysisTask()
    }

    private void preparePmdTask() {
        def task = project.task(PMD_TASK_NAME)
        task.description = 'Runs PMD analysis on project'
        task.group = AMEBA_ANALYSIS
        project.configurations.add('pmdConf')
        project.dependencies.add('pmdConf', 'pmd:pmd:4.3')
        task << { new PMDTask(project).runPMD() }
    }

    private void prepareCpdTask() {
        def task = project.task(CPD_TASK_NAME)
        task.description = 'Runs CPD (duplicated code) analysis on project'
        task.group = AMEBA_ANALYSIS
        task << { new CPDTask(project).runCPD() }
    }

    private void prepareFindbugsTask() {
        def task = project.task(FINDBUGS_TASK_NAME)
        task.description = "Runs Findbugs analysis on project"
        task.group = AMEBA_ANALYSIS
        project.configurations.add('findbugsConf')
        project.dependencies.add('findbugsConf', 'com.google.code.findbugs:findbugs:2.0.1')
        project.dependencies.add('findbugsConf', 'com.google.code.findbugs:findbugs-ant:2.0.1')
        task << { new FindBugsTask(project).runFindbugs() }
        task.dependsOn(CLASSES_TASK_NAME)
    }

    private void prepareCheckstyleTask() {
        def task = project.task(CHECKSTYLE_TASK_NAME)
        task.description = 'Runs Checkstyle analysis on project'
        task.group = AMEBA_ANALYSIS
        project.configurations.add('checkstyleConf')
        project.dependencies.add('checkstyleConf', 'com.puppycrawl.tools:checkstyle:5.6')
        task << { new CheckStyleTask(project).runCheckStyle() }
        task.dependsOn(CLASSES_TASK_NAME)
    }

    private void prepareAnalysisTask() {
        def task = project.task(ANALYSIS_TASK_NAME)
        task.description = 'Runs all analysis on project'
        task.group = AMEBA_ANALYSIS
        task.dependsOn(FINDBUGS_TASK_NAME)
        task.dependsOn(PMD_TASK_NAME)
        task.dependsOn(CPD_TASK_NAME)
        task.dependsOn(CHECKSTYLE_TASK_NAME)
    }
}
