package com.apphance.flow.plugins.android.analysis

import com.apphance.flow.TestUtils
import com.apphance.flow.configuration.android.AndroidAnalysisConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.plugins.android.analysis.tasks.CPDTask
import com.apphance.flow.plugins.android.analysis.tasks.LintTask
import org.gradle.api.file.FileCollection
import spock.lang.Specification

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_ANALYSIS
import static org.gradle.testfixtures.ProjectBuilder.builder

@Mixin([TestUtils])
class AndroidAnalysisPluginSpec extends Specification {

    def 'tasks defined in plugin available when configuration is active'() {
        given:
        def project = builder().build()
        def plugin = new AndroidAnalysisPlugin()
        def conf = GroovyStub(AndroidAnalysisConfiguration)
        conf.isEnabled() >> true
        conf.pmdRules >> new FileProperty()
        conf.findbugsExclude >> new FileProperty()
        conf.checkstyleConfigFile >> new FileProperty()

        def variantsConf = GroovyStub(AndroidVariantsConfiguration)
        def mainVariant = GroovyStub(AndroidVariantConfiguration)
        mainVariant.tmpDir >> temporaryDir
        mainVariant.buildTaskName >> 'debug'
        variantsConf.main >> mainVariant

        plugin.androidVariantsConf = variantsConf
        plugin.analysisConf = conf

        when:
        plugin.apply(project)

        then: 'check plugins are applied'
        project.plugins.findPlugin('java')
        project.plugins.findPlugin('pmd')
        project.plugins.findPlugin('checkstyle')
        project.plugins.findPlugin('findbugs')

        !project.plugins.findPlugin('groovy')
        !project.plugins.findPlugin('scala')

        and: 'config files are prepared'
        plugin.rulesDir.exists()
        plugin.pmdRules.exists()
        plugin.findbugsExclude.exists()
        plugin.checkstyleConfigFile.exists()

        and: 'configs set in analysis plugins configurations'
        project.pmd.ruleSetFiles instanceof FileCollection
        (project.pmd.ruleSetFiles as FileCollection).files == [plugin.pmdRules] as Set
        project.findbugs.excludeFilter == plugin.findbugsExclude
        project.checkstyle.configFile == plugin.checkstyleConfigFile

        and: 'sources sets properly configured'
        project.sourceSets.main.java.srcDirs == [new File(project.rootDir, 'src'), new File(project.rootDir, 'variants')] as Set
        project.sourceSets.test.java.srcDirs == [new File(project.rootDir, 'test')] as Set
        project.sourceSets.main.output.classesDir == new File(mainVariant.tmpDir, 'bin/classes')

        and: 'tasks added'
        !project.tasks.findByName('nonexistingTask')
        [CPDTask.NAME, LintTask.NAME, 'check', 'pmdMain', 'pmdTest', 'checkstyleMain', 'checkstyleTest', 'findbugsMain', 'findbugsTest'].
                every { project.tasks.findByName(it) }
        project.tasks[CPDTask.NAME].group == FLOW_ANALYSIS.name()
        project.tasks[LintTask.NAME].group == FLOW_ANALYSIS.name()
        project.tasks['check'].group == 'verification'

        and: 'task dependencies'
        project.check.dependsOn CPDTask.NAME
        project.check.dependsOn LintTask.NAME

        project.lint.dependsOn mainVariant.buildTaskName
        project.findbugsMain.dependsOn mainVariant.buildTaskName

        and: 'default java plugin tasks disabled'
        project.with { [compileJava, compileTestJava, processResources, processTestResources, test, classes, testClasses, findbugsTest].every { !it.enabled } }

        and: 'ignoreFailures flag in analysis plugins'
        project.with { [checkstyle, findbugs, pmd, cpd].every { it.ignoreFailures } }
    }

    def 'no tasks available when configuration is inactive'() {
        given:
        def project = builder().build()
        def plugin = new AndroidAnalysisPlugin()
        def conf = GroovyStub(AndroidAnalysisConfiguration)
        conf.isEnabled() >> false
        plugin.analysisConf = conf

        when:
        plugin.apply(project)

        then:
        !['java', 'pmd', 'checkstyle', 'findbugs'].collect { project.plugins.findPlugin(it) }.grep()
        plugin.rulesDir == null
        plugin.pmdRules == null
        plugin.findbugsExclude == null
    }
}
