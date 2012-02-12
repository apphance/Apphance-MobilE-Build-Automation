package com.apphance.ameba

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging

import com.apphance.ameba.AmebaCommonBuildTaskGroups;
import com.apphance.ameba.PropertyCategory;


class AbstractPrepareSetupTask extends DefaultTask {

    public static final String GENERATED_GRADLE_PROPERTIES = 'generated.gradle.properties'
    Logger logger = Logging.getLogger(AbstractPrepareSetupTask.class)
    String propertyDescription
    Class<? extends Enum> clazz

    AbstractPrepareSetupTask(Class<? extends Enum> clazz) {
        use (PropertyCategory) {
            this.propertyDescription = clazz.getField('DESCRIPTION').get(null)
            this.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
            this.description = "Walks you through setup of the ${propertyDescription} of the project."
            //inject myself as dependency for umbrella prepareSetup
            project.prepareSetup.dependsOn(this)
            this.dependsOn(project.readProjectConfiguration)
        }
    }

    void appendToGeneratedPropertyString(String propertyString) {
        use (PropertyCategory) {
            String oldValue = project.readProperty(GENERATED_GRADLE_PROPERTIES, '')
            String newValue = oldValue + propertyString
            project[GENERATED_GRADLE_PROPERTIES] = newValue
        }
    }
}
