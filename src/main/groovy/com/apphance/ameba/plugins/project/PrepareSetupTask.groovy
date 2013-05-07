package com.apphance.ameba.plugins.project

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.reader.ConversationManager
import com.apphance.ameba.configuration.reader.PropertyPersister
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_SETUP

class PrepareSetupTask extends DefaultTask {

    static final NAME = 'prepareSetup'
    String group = AMEBA_SETUP
    String description = 'Prepares configuration (ameba.properties)'

    @Inject
    Map<Integer, AbstractConfiguration> configurations
    @Inject
    PropertyPersister propertyPersister
    @Inject
    ConversationManager conversationManager


    @TaskAction
    void prepareSetup() {
        Collection<AbstractConfiguration> sorted = configurations.sort().values()
        conversationManager.resolveConfigurations(sorted)
        propertyPersister.save(sorted)
    }
}
