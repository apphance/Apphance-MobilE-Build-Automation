package com.apphance.ameba.android.plugins.analysis

import com.apphance.ameba.android.plugins.analysis.tasks.CPDTask
import com.apphance.ameba.android.plugins.analysis.tasks.CheckstyleTask
import com.apphance.ameba.android.plugins.analysis.tasks.FindbugsTask
import com.apphance.ameba.android.plugins.analysis.tasks.PMDTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

import static com.apphance.ameba.AmebaCommonBuildTaskGroups.AMEBA_ANALYSIS

/**
 * Provides static code analysis.
 *
 */
class AndroidAnalysisPlugin implements Plugin<Project> {

    public static final String FINDBUGS_HOME_DIR_PROPERTY = 'findbugs.home.dir'
    public static final String FINDBUGS_DEFAULT_HOME = '/var/lib/analysis/findbugs'

    private Project project

    @Override
    public void apply(Project project) {
        this.project = project
        def androidAnalysisConvention = new AndroidAnalysisConvention()
        project.convention.plugins.put('androidAnalysis', androidAnalysisConvention)
        preparePmdTask()
        prepareCpdTask()
        prepareFindbugsTask()
        prepareCheckstyleTask()
        prepareAnalysisTask()
    }

    private void preparePmdTask() {
        Task task = project.task('pmd')
        task.description = 'Runs PMD analysis on project'
        task.group = AMEBA_ANALYSIS
        project.configurations.add('pmdConf')
        project.dependencies.add('pmdConf', 'pmd:pmd:4.2.6')
        task.doLast { new PMDTask(project).runPMD() }
    }

    private void prepareCpdTask() {
        Task task = project.task('cpd')
        task.description = 'Runs CPD (duplicated code) analysis on project'
        task.group = AMEBA_ANALYSIS
        task.doLast { new CPDTask(project).runCPD() }
    }

    private void prepareFindbugsTask() {
        def task = project.task('findbugs')
        task.description = "Runs Findbugs analysis on project"
        task.group = AMEBA_ANALYSIS
        project.configurations.add('findbugsConf')
        project.dependencies.add('findbugsConf', 'com.google.code.findbugs:findbugs:2.0.0')
        project.dependencies.add('findbugsConf', 'com.google.code.findbugs:findbugs-ant:2.0.0')
        task.doLast { new FindbugsTask(project).runFindbugs() }
        task.dependsOn('classes')
    }

    private void prepareCheckstyleTask() {
        Task task = project.task('checkstyle')
        task.description = 'Runs Checkstyle analysis on project'
        task.group = AMEBA_ANALYSIS
        project.configurations.add('checkstyleConf')
        project.dependencies.add('checkstyleConf', 'checkstyle:checkstyle:5.0')
        task.doLast { new CheckstyleTask(project).runCheckstyle() }
        task.dependsOn('classes')
    }

    private void prepareAnalysisTask() {
        Task task = project.task('analysis')
        task.description = 'Runs all analysis on project'
        task.group = AMEBA_ANALYSIS
        task.dependsOn('findbugs')
        task.dependsOn('pmd')
        task.dependsOn('cpd')
        task.dependsOn('checkstyle')
    }

    static class AndroidAnalysisConvention {
        static public final String DESCRIPTION =
            """The convention provides base URL where analysis configuration files are placed.
The configuration files can be either internal (no configuration needed)
or taken from local configuration directory (config/analysis)
or retrieved using base URL specified."""
        def String baseAnalysisConfigUrl = null

        def androidAnalysis(Closure close) {
            close.delegate = this
            close.run()
        }
    }

    static public final String DESCRIPTION =
        """This plugin provides capability of running basic static code analysis on android.

It provides analysis task, that executes checkstyle, findbugs, pmd tasks (soon also lint).

Note that the findbugs plugin requires findbugs to be installed and it's home has to be configured.
By default the home of findbugs is in '${FINDBUGS_DEFAULT_HOME}' but it can be configured
by specifying gradle's property: ${FINDBUGS_HOME_DIR_PROPERTY}. Currently supported findbugs version is 2.0.0 and
this version has to be installed.

The Easiest way it is to add it in gradle.properties or specified in organisation-specific conventions:
<code>
this['${FINDBUGS_HOME_DIR_PROPERTY}'] = 'SPECIFY HOME DIRECTORY'
</code>

Configuration files for all analysis taks are retrieved from internal configuration.
The configuration files can also be present in local config directory in the project (for project-specific configuration)
They can also be downloaded remotely using http if specified in convention - this is for organisation-wide setup for many projects.
The convention or local config files (if present) take precedence over the internal configuration, however if specific
configuration file cannot be found, the internal version is used.

The structure of the directory/URL is as follows:
<code>
[config/analysis] directory or base URL
+---+- checkstyle.xml : main checkstyle configuration
    +- checkstyle-suppressions.xml : suppressions from checkstyle
    +- checkstyle-local-suppressions.xml : additional local suppressions
    +- findbugs-exclude.xml : findbugs excludes configuration
    +- pmd-rules.xml : pmd rules
</code>
Example of configuration files that can be used as starting point for your local/project configuration
can be found
<a href="https://github.com/apphance/Apphance-MobilE-Build-Automation/tree/master/src/main/resources/com/apphance/ameba/android/plugins/analysis">here</a>
"""
}
