package com.apphance.ameba

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging

import com.apphance.ameba.AmebaCommonBuildTaskGroups;
import com.apphance.ameba.PropertyCategory;


class AbstractPrepareSetupTask<T extends Enum> extends DefaultTask {

    Logger logger = Logging.getLogger(AbstractPrepareSetupTask.class)
    final String propertyDescription
    final Class<T> clazz

    AbstractPrepareSetupTask(Class<T> clazz) {
        use (PropertyCategory) {
            this.propertyDescription = clazz.getField('DESCRIPTION').get(null)
            this.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
            this.description = "Walks you through setup of the ${propertyDescription} of the project."
            //inject myself as dependency for umbrella prepareSetup
            project.prepareSetup.dependsOn(this)
        }
    }
}
