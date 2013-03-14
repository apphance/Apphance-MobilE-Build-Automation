package com.apphance.ameba.android.plugins.analysis

import org.gradle.api.plugins.JavaPlugin
import spock.lang.Specification

import static com.apphance.ameba.AmebaCommonBuildTaskGroups.AMEBA_ANALYSIS
import static org.gradle.testfixtures.ProjectBuilder.builder

class AndroidAnalysisPluginSpec extends Specification {

    def "plugin tasks' graph configured correctly"() {
        given:
        def project = builder().build()

        when:
        project.plugins.apply(JavaPlugin)
        project.plugins.apply(AndroidAnalysisPlugin)

        then: 'every single task is in correct group'
        project.tasks['pmd'].group == AMEBA_ANALYSIS
        project.tasks['cpd'].group == AMEBA_ANALYSIS
        project.tasks['findbugs'].group == AMEBA_ANALYSIS
        project.tasks['checkstyle'].group == AMEBA_ANALYSIS
        project.tasks['analysis'].group == AMEBA_ANALYSIS

        then: 'configurations for tasks were added properly'
        project.configurations.pmdConf
        project.configurations.findbugsConf
        project.configurations.checkstyleConf

        then: 'external dependencies configured correctly'
        project.dependencies.configurationContainer.pmdConf.dependencies
        project.dependencies.configurationContainer.pmdConf.dependencies.find {
            it.group == 'pmd' && it.name == 'pmd' && it.version == '4.2.6'
        }

        project.dependencies.configurationContainer.findbugsConf.dependencies
        project.dependencies.configurationContainer.findbugsConf.dependencies.find {
            it.group == 'com.google.code.findbugs' && it.name == 'findbugs' && it.version == '2.0.0'
        }
        project.dependencies.configurationContainer.findbugsConf.dependencies.find {
            it.group == 'com.google.code.findbugs' && it.name == 'findbugs-ant' && it.version == '2.0.0'
        }

        project.dependencies.configurationContainer.checkstyleConf.dependencies
        project.dependencies.configurationContainer.checkstyleConf.dependencies.find {
            it.group == 'checkstyle' && it.name == 'checkstyle' && it.version == '5.0'
        }

        and: 'task dependencies configured correctly'
        project.tasks['analysis'].dependsOn.containsAll('findbugs', 'cpd', 'pmd', 'checkstyle')
        project.tasks['findbugs'].dependsOn.containsAll('classes')
        project.tasks['checkstyle'].dependsOn.containsAll('classes')
    }
}
