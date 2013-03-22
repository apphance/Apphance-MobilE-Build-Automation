package com.apphance.ameba.plugins.projectconfiguration.tasks

import com.apphance.ameba.plugins.projectconfiguration.ShowConventionHelper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_SETUP

/**
 * Task for showing all conventions.
 *
 */
class ShowConventionsTask extends DefaultTask {

    ShowConventionHelper showConventionHelper = new ShowConventionHelper()

    ShowConventionsTask() {
        this.description = 'Shows all available conventions'
        this.group = AMEBA_SETUP
    }

    @TaskAction
    void showSetup() {
        StringBuilder sb = new StringBuilder()
        project.convention.plugins.each { pluginName, pluginConventionObject ->
            if (!(pluginName in ['reportingBase'])) {
                showConventionHelper.getConventionObject(sb, pluginName, pluginConventionObject)
            }
        }
        println sb
    }
}