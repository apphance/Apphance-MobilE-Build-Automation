package com.apphance.ameba.plugins.project.tasks

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.reader.ConfigurationWizard
import com.apphance.ameba.configuration.reader.PropertyPersister
import com.apphance.ameba.configuration.reader.PropertyReader
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.configuration.reader.GradlePropertiesPersister.FLOW_PROP_FILENAME
import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_SETUP

class PrepareSetupTask extends DefaultTask {

    static final NAME = 'prepareSetup'
    String group = AMEBA_SETUP
    String description = "Prepares configuration (${FLOW_PROP_FILENAME}). Can be used in non-interactive mode \"-Dnoninteractive\""

    @Inject Map<Integer, AbstractConfiguration> configurations
    @Inject PropertyPersister propertyPersister
    @Inject ConfigurationWizard conversationManager
    @Inject PropertyReader propertyReader

    @TaskAction
    void prepareSetup() {
        Collection<AbstractConfiguration> sorted = configurations.sort().values()
        conversationManager.interactiveMode = propertyReader.systemProperty('noninteractive') == null
        conversationManager.resolveConfigurations(sorted)
        propertyPersister.save(sorted)
    }
}
