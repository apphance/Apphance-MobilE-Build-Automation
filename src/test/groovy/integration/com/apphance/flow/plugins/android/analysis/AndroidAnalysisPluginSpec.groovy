package com.apphance.flow.plugins.android.analysis

import com.apphance.flow.TestUtils
import com.apphance.flow.configuration.android.AndroidAnalysisConfiguration
import com.apphance.flow.configuration.android.AndroidTestConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.plugins.android.analysis.tasks.CPDTask
import com.apphance.flow.plugins.android.analysis.tasks.LintTask
import com.apphance.flow.plugins.android.buildplugin.tasks.CompileAndroidTask
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import spock.lang.Specification
import spock.lang.Unroll

import static org.gradle.testfixtures.ProjectBuilder.builder

@Mixin([TestUtils])
class AndroidAnalysisPluginSpec extends Specification {

    def project = builder().build()

    @Unroll
    def 'tasks defined in plugin available when configuration is active'() {
        given:
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
        plugin.androidTestConf = GroovyStub(AndroidTestConfiguration)
        plugin.androidTestConf.enabled >> testConfEnabled

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
        project.sourceSets.test.output.classesDir == new File(mainVariant.tmpDir, 'bin/testClasses')

        and: 'tasks added'
        !project.tasks.findByName('nonexistingTask')
        [CPDTask.NAME, LintTask.NAME, 'check', 'pmdMain', 'pmdTest', 'checkstyleMain', 'checkstyleTest', 'findbugsMain', 'findbugsTest'].
                every { project.tasks.findByName(it) }

        [CPDTask.NAME, LintTask.NAME, 'check'].every { project.tasks[it].group == 'verification' }

        and: 'task dependencies'
        project.tasks['check'].dependsOn.grep(CPDTask)
        project.tasks['check'].dependsOn.grep(LintTask)
        mainVariant.buildTaskName in project.tasks[LintTask.NAME].dependsOn
        mainVariant.buildTaskName in project.tasks['findbugsMain'].dependsOn
        !(testConfEnabled <=> mainVariant.testTaskName in project.tasks['findbugsTest'].dependsOn)

        project.findbugsTest.enabled == testConfEnabled

        and: 'default java plugin tasks disabled'
        project.with { [compileJava, compileTestJava, processResources, processTestResources, test, classes, testClasses].every { !it.enabled } }

        and: 'ignoreFailures flag in analysis plugins'
        project.with { [checkstyle, findbugs, pmd, cpd].every { it.ignoreFailures } }

        where:
        testConfEnabled << [true, false]
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
