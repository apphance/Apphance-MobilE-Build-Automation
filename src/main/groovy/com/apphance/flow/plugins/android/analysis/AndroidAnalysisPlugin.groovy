package com.apphance.flow.plugins.android.analysis

import com.apphance.flow.configuration.android.AndroidAnalysisConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
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

            project.apply plugin: 'java'
            project.apply plugin: 'pmd'
            project.apply plugin: 'checkstyle'
            project.apply plugin: 'findbugs'

            project.compileJava.enabled = false
            project.compileTestJava.enabled = false
            project.processResources.enabled = false
            project.processTestResources.enabled = false

            project.checkstyleMain.ignoreFailures = true
            project.findbugsMain.ignoreFailures = true
            project.pmd.ignoreFailures = true

            def mainVariant = androidVariantsConf.variants.find { it.name == androidVariantsConf.mainVariant }

            project.findbugsMain.dependsOn mainVariant.buildTaskName
            project.findbugsTest.dependsOn mainVariant.buildTaskName

            project.sourceSets.main.java { srcDir 'src' }
            def variantDir = FileManager.relativeTo(project.rootDir, mainVariant.tmpDir)
            project.sourceSets.main.output.classesDir = variantDir + '/bin/classes'
        }
    }
}
