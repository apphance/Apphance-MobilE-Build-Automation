package com.apphance.flow.plugins.android.analysis

import com.apphance.flow.configuration.android.AndroidAnalysisConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.plugins.android.analysis.tasks.CPDTask
import com.apphance.flow.plugins.android.analysis.tasks.LintTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.quality.FindBugs

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

    File rulesDir = new File('build/analysis-rules')
    File pmdRules
    File findbugsExclude
    File checkstyleConfigFile

    @Override
    void apply(Project project) {
        if (analysisConf.isEnabled()) {
            logger.lifecycle "Applying plugin ${this.class.simpleName}"

            prepareRuleFiles()

            def mainVariant = androidVariantsConf.variants.find { it.name == androidVariantsConf.mainVariant }
            def mainVariantDir = relativeTo(project.rootDir, mainVariant.tmpDir)

            project.with {
                apply plugin: 'java'
                apply plugin: 'pmd'
                apply plugin: 'checkstyle'
                apply plugin: 'findbugs'

                pmd.ruleSetFiles = files(pmdRules)

                tasks.withType(FindBugs) {
                    excludeFilter = findbugsExclude
                }

                sourceSets.main.java.srcDirs = ['src', 'variants']
                sourceSets.test.java.srcDirs = ['test']
                sourceSets.main.output.classesDir = mainVariantDir + '/bin/classes'

                def cpd = task(CPDTask.NAME, type: CPDTask) as CPDTask
                cpd.source(sourceSets.main.java.srcDirs)

                def lint = task(LintTask.NAME, type: LintTask)

                check.dependsOn cpd, lint
                [findbugsMain, findbugsTest, lint]*.dependsOn(mainVariant.buildTaskName)

                [compileJava, compileTestJava, processResources, processTestResources, test, classes, testClasses].each { it.enabled = false }
                [checkstyle, findbugs, pmd, cpd].each { it.ignoreFailures = true }
            }
        }
    }

    void prepareRuleFiles() {
        rulesDir.mkdirs()
        pmdRules = prepareConfigFile(analysisConf.pmdRules.value, 'pmd-rules.xml')
        findbugsExclude = prepareConfigFile(analysisConf.findbugsExclude.value, 'findbugs-exclude.xml')
        checkstyleConfigFile = prepareConfigFile(analysisConf.checkstyleConfigFile.value, 'checkstyle.xml')
    }

    File prepareConfigFile(File valueFormConf, String filename) {
        valueFormConf ?: {
            def stream = this.class.getResourceAsStream("/com/apphance/flow/plugins/android/analysis/tasks/$filename")
            File rules = new File(rulesDir, filename)
            rules.text = stream.text
            rules
        }()
    }
}
