package com.apphance.ameba.plugins.release

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AmebaCommonBuildTaskGroups;
import com.apphance.ameba.ProjectConfiguration;
import com.apphance.ameba.ProjectHelper;
import com.apphance.ameba.PropertyManager;
import com.apphance.ameba.plugins.projectconfiguration.PrepareBaseSetupTask;

class PrepareReleaseSetupTask extends DefaultTask {

    Logger logger = Logging.getLogger(PrepareBaseSetupTask.class)
    ProjectConfiguration conf

    PrepareReleaseSetupTask() {
        this.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        this.description = 'Walks you through the release part of setup of the project.'
        this.conf = new ProjectConfiguration()
        //inject myself as dependency for umbrella prepareSetup
        project.prepareSetup.dependsOn(this)
        //		this.logLevel = LogLevel.QUIET
        this.logging.setLevel(LogLevel.QUIET)
    }

    @TaskAction
    void prepareSetup() {
        System.out.println("""#######################
# Preparing release setup
#######################""")
        System.out.println('Type values for properties')
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in))

        for (ProjectReleaseProperty property : ProjectReleaseProperty.values()) {
            ProjectHelper.getProjectPropertyFromUser(project, property.propertyName, property.description, null, false, br)
        }

        File propsFile = new File('gradle.props')
        propsFile << PropertyManager.listPropertiesAsString(project, ProjectReleaseProperty.class, true)
    }
}
