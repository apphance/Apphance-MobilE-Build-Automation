package com.apphance.flow.plugins.android.analysis

import com.apphance.flow.configuration.android.AndroidAnalysisConfiguration
import com.apphance.flow.configuration.android.AndroidTestConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.plugins.android.analysis.tasks.CPDTask
import com.apphance.flow.plugins.android.analysis.tasks.LintTask
import com.apphance.flow.plugins.project.tasks.VerifySetupTask
import com.apphance.flow.util.FlowUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.flow.util.file.FileManager.relativeTo
import static org.gradle.api.logging.Logging.getLogger

@Mixin(FlowUtils)
class AndroidAnalysisPlugin implements Plugin<Project> {

    def logger = getLogger(this.class)

    @Inject AndroidAnalysisConfiguration analysisConf
    @Inject AndroidVariantsConfiguration androidVariantsConf
    @Inject AndroidTestConfiguration androidTestConf

    File rulesDir = temporaryDir
    File pmdRules
    File findbugsExclude
    File checkstyleConfigFile

    def static analysisTasks = [CPDTask.NAME, LintTask.NAME, 'check', 'pmdMain', 'pmdTest', 'checkstyleMain', 'checkstyleTest', 'findbugsMain', 'findbugsTest']

    @Override
    void apply(Project project) {
        if (analysisConf.isEnabled()) {
            logger.lifecycle "Applying plugin ${this.class.simpleName}"

            prepareRuleFiles()

            def mainVariantDir = relativeTo(project.rootDir, androidVariantsConf.mainVariant.tmpDir)

            project.with {
                apply plugin: 'java'
                apply plugin: 'pmd'
                apply plugin: 'checkstyle'
                apply plugin: 'findbugs'

                pmd.ruleSetFiles = files(pmdRules)
                findbugs.excludeFilter = findbugsExclude
                checkstyle.configFile = checkstyleConfigFile

                sourceSets.main.java.srcDirs = ['src', 'variants']
                sourceSets.test.java.srcDirs = ['test']
                sourceSets.main.output.classesDir = mainVariantDir + '/bin/classes'
                sourceSets.test.output.classesDir = mainVariantDir + '/bin/testClasses'

                def cpd = task(CPDTask.NAME, type: CPDTask) as CPDTask
                cpd.source(sourceSets.main.java.srcDirs)

                def lint = task(LintTask.NAME, type: LintTask)

                check.dependsOn cpd, lint
                [findbugsMain, lint]*.dependsOn androidVariantsConf.mainVariant.buildTaskName

                findbugsTest.enabled = androidTestConf?.enabled
                if (findbugsTest.enabled) {
                    findbugsTest.dependsOn androidVariantsConf.mainVariant.testTaskName
                }

                [compileJava, compileTestJava, processResources, processTestResources, test, classes, testClasses].each { it.enabled = false }
                [checkstyle, findbugs, pmd, cpd].each { it.ignoreFailures = true }
            }

            analysisTasks.each { project.tasks.findByName(it)?.dependsOn(VerifySetupTask.NAME) }
        }
    }

    void prepareRuleFiles() {
        pmdRules = prepareConfigFile(analysisConf.pmdRules.value, 'pmd-rules.xml')
        findbugsExclude = prepareConfigFile(analysisConf.findbugsExclude.value, 'findbugs-exclude.xml')
        checkstyleConfigFile = prepareConfigFile(analysisConf.checkstyleConfigFile.value, 'checkstyle.xml')

        logger.lifecycle "Pmd rules file: $pmdRules.absolutePath findbugs exclude file: $findbugsExclude.absolutePath " +
                "checkstyle config file: $checkstyleConfigFile.absolutePath"
    }

    File prepareConfigFile(File valueFormConf, String filename) {
        valueFormConf ?: {
            def stream = this.class.getResourceAsStream("/com/apphance/flow/plugins/android/analysis/tasks/$filename")
            File rules = new File(rulesDir, filename)
            rules.text = stream.text
            logger.lifecycle "Created rule file $rules.absolutePath"
            rules
        }()
    }
}
