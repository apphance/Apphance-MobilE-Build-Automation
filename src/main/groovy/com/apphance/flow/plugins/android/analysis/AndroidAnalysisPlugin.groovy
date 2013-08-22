package com.apphance.flow.plugins.android.analysis

import com.apphance.flow.configuration.android.AndroidAnalysisConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.plugins.android.analysis.tasks.CPDTask
import com.apphance.flow.util.file.FileManager
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

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
            def variantDir = FileManager.relativeTo(project.rootDir, mainVariant.tmpDir)

            project.with {
                apply plugin: 'java'
                apply plugin: 'pmd'
                apply plugin: 'checkstyle'
                apply plugin: 'findbugs'

                [compileJava, compileTestJava, processResources, processTestResources].each { it.enabled = false }
                [checkstyle, findbugs, pmd].each { it.ignoreFailures = true }
                pmd.toolVersion = '5.0.4'

                findbugsMain.dependsOn mainVariant.buildTaskName
                findbugsTest.dependsOn mainVariant.buildTaskName

                sourceSets.main.java.srcDirs = ['src', 'variants']
                sourceSets.test.java.srcDirs = ['test']
                sourceSets.main.output.classesDir = variantDir + '/bin/classes'

                def cpd = task(CPDTask.NAME, type: CPDTask) as CPDTask
                cpd.source(sourceSets.main.java.srcDirs)
                cpd.ignoreFailures = true
            }
        }
    }
}
