package com.apphance.ameba.plugins.android.analysis

import com.apphance.ameba.configuration.android.AndroidAnalysisConfiguration
import com.apphance.ameba.plugins.android.analysis.tasks.CPDTask
import com.apphance.ameba.plugins.android.analysis.tasks.CheckstyleTask
import com.apphance.ameba.plugins.android.analysis.tasks.FindBugsTask
import com.apphance.ameba.plugins.android.analysis.tasks.PMDTask
import com.apphance.ameba.plugins.android.buildplugin.tasks.CompileAndroidTask
import spock.lang.Specification

import static com.apphance.ameba.plugins.FlowTasksGroups.FLOW_ANALYSIS
import static org.gradle.api.plugins.JavaPlugin.CLASSES_TASK_NAME
import static org.gradle.testfixtures.ProjectBuilder.builder

class AndroidAnalysisPluginSpec extends Specification {

    def 'tasks defined in plugin available when configuration is active'() {
        given:
        def project = builder().build()

        and:
        project.tasks.add(CLASSES_TASK_NAME)

        and:
        def aap = new AndroidAnalysisPlugin()

        and:
        def aac = Mock(AndroidAnalysisConfiguration)
        aac.isEnabled() >> true
        aap.analysisConf = aac

        when:
        aap.apply(project)

        then: 'every single task is in correct group'
        project.tasks[PMDTask.NAME].group == FLOW_ANALYSIS.name()
        project.tasks[CPDTask.NAME].group == FLOW_ANALYSIS.name()
        project.tasks[FindBugsTask.NAME].group == FLOW_ANALYSIS.name()
        project.tasks[CheckstyleTask.NAME].group == FLOW_ANALYSIS.name()
        project.tasks['analysis'].group == FLOW_ANALYSIS.name()

        then: 'configurations for tasks were added properly'
        project.configurations.pmdConf
        project.configurations.findbugsConf
        project.configurations.checkstyleConf

        then: 'external dependencies configured correctly'
        project.dependencies.configurationContainer.pmdConf.dependencies
        project.dependencies.configurationContainer.pmdConf.dependencies.find {
            it.group == 'pmd' && it.name == 'pmd' && it.version == '4.3'
        }

        project.dependencies.configurationContainer.findbugsConf.dependencies
        project.dependencies.configurationContainer.findbugsConf.dependencies.find {
            it.group == 'com.google.code.findbugs' && it.name == 'findbugs' && it.version == '2.0.1'
        }
        project.dependencies.configurationContainer.findbugsConf.dependencies.find {
            it.group == 'com.google.code.findbugs' && it.name == 'findbugs-ant' && it.version == '2.0.1'
        }

        project.dependencies.configurationContainer.checkstyleConf.dependencies
        project.dependencies.configurationContainer.checkstyleConf.dependencies.find {
            it.group == 'com.puppycrawl.tools' && it.name == 'checkstyle' && it.version == '5.6'
        }

        and: 'task dependencies configured correctly'
        project.tasks['analysis'].dependsOn.flatten().containsAll(FindBugsTask.NAME,
                CPDTask.NAME,
                PMDTask.NAME,
                CheckstyleTask.NAME)
        project.tasks[FindBugsTask.NAME].dependsOn.contains(CompileAndroidTask.NAME)
        project.tasks[CheckstyleTask.NAME].dependsOn.contains(CompileAndroidTask.NAME)
    }

    def 'no tasks available when configuration is inactive'() {
        given:
        def project = builder().build()

        and:
        def aap = new AndroidAnalysisPlugin()

        and:
        def aac = Mock(AndroidAnalysisConfiguration)
        aac.isEnabled() >> false
        aap.analysisConf = aac

        when:
        aap.apply(project)

        then:
        !project.configurations.findByName('pmdConf')
        !project.configurations.findByName('findbugsConf')
        !project.configurations.findByName('checkstyleConf')

        then:
        !project.getTasksByName(PMDTask.NAME, false)
        !project.getTasksByName(CPDTask.NAME, false)
        !project.getTasksByName(FindBugsTask.NAME, false)
        !project.getTasksByName(CheckstyleTask.NAME, false)
        !project.getTasksByName('analysis', false)
    }
}
