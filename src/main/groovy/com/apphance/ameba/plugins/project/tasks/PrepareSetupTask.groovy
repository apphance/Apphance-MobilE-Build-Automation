package com.apphance.ameba.plugins.project.tasks

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.reader.ConfigurationWizard
import com.apphance.ameba.configuration.reader.PropertyPersister
import com.apphance.ameba.configuration.reader.PropertyReader
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.configuration.reader.GradlePropertiesPersister.FLOW_PROP_FILENAME
import static com.apphance.ameba.plugins.FlowTasksGroups.FLOW_SETUP

class PrepareSetupTask extends DefaultTask {

    static final NAME = 'prepareSetup'
    String group = FLOW_SETUP
    String description = "Prepares configuration (${FLOW_PROP_FILENAME}). Can be used in non-interactive mode '-Dnoninteractive' or '-Dni'"

    @Inject Map<Integer, AbstractConfiguration> configurations
    @Inject PropertyPersister propertyPersister
    @Inject ConfigurationWizard configurationWizard
    @Inject PropertyReader propertyReader

    @TaskAction
    void prepareSetup() {
        Collection<AbstractConfiguration> sorted = configurations.sort().values()
        configurationWizard.interactiveMode = ['ni', 'noninteractive'].every { System.getProperty(it) == null }
        println "Running configuration wizard. Interactive mode = ${configurationWizard.interactiveMode}"
        configurationWizard.resolveConfigurations(sorted)
        propertyPersister.save(sorted)
    }
}
