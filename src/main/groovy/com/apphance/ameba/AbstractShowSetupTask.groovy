package com.apphance.ameba


import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction;

abstract class AbstractShowSetupTask extends DefaultTask{
    Logger logger = Logging.getLogger(AbstractShowSetupTask.class)
    final String propertyDescription
    final Class<? extends Enum> clazz

    public AbstractShowSetupTask(Class<? extends Enum> clazz) {
        this.propertyDescription = clazz.getField('DESCRIPTION').get(null)
        this.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        this.clazz = clazz
        this.description = "Shows ${propertyDescription} of the project"
        // inject myself before the default
        this.dependsOn(project.readProjectConfiguration)
        project.showSetup.dependsOn(this)
    }

    @TaskAction
    public void showSetup() {
        use (PropertyCategory) {
            System.out.print(project.listPropertiesAsString(clazz, true))
        }
    }
}
