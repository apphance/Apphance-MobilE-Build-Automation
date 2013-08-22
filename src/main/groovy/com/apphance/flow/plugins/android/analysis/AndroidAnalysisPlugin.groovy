package com.apphance.flow.plugins.android.analysis

import com.apphance.flow.configuration.android.AndroidAnalysisConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.plugins.android.analysis.tasks.CPDTask
import com.apphance.flow.plugins.android.analysis.tasks.LintTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.flow.util.file.FileManager.relativeTo
import static org.gradle.api.logging.Logging.getLogger

/**
 * Provides static code analysis.
 */
class AndroidAnalysisPlugin implements Plugin<Project> {

    def logger = getLogger(this.class)

    @Inject AndroidAnalysisConfiguration analysisConf
    @Inject AndroidVariantsConfiguration androidVariantsConf

    @Override
    void apply(Project project) {
        if (analysisConf.isEnabled()) {
            logger.lifecycle "Applying plugin ${this.class.simpleName}"

            def mainVariant = androidVariantsConf.variants.find { it.name == androidVariantsConf.mainVariant }
            def mainVariantDir = relativeTo(project.rootDir, mainVariant.tmpDir)

            project.with {
                apply plugin: 'java'
                apply plugin: 'pmd'
                apply plugin: 'checkstyle'
                apply plugin: 'findbugs'

                pmd.toolVersion = '5.0.4'

                sourceSets.main.java.srcDirs = ['src', 'variants']
                sourceSets.test.java.srcDirs = ['test']
                sourceSets.main.output.classesDir = mainVariantDir + '/bin/classes'

                def cpd = task(CPDTask.NAME, type: CPDTask) as CPDTask
                cpd.source(sourceSets.main.java.srcDirs)

                def lint = task(LintTask.NAME, type: LintTask)

                check.dependsOn cpd, lint
                [findbugsMain, findbugsTest, lint]*.dependsOn(mainVariant.buildTaskName)

                [compileJava, compileTestJava, processResources, processTestResources].each { it.enabled = false }
                [checkstyle, findbugs, pmd, cpd].each { it.ignoreFailures = true }
            }
        }
    }
}
