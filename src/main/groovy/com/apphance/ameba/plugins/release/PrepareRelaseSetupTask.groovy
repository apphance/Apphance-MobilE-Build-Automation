package com.apphance.ameba.plugins.release

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AmebaCommonBuildTaskGroups;
import com.apphance.ameba.ProjectConfiguration;
import com.apphance.ameba.PropertyCategory;

class PrepareReleaseSetupTask extends DefaultTask {

    Logger logger = Logging.getLogger(PrepareReleaseSetupTask.class)
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
        logger.lifecycle('Preparing release setup')
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in))
        use (PropertyCategory) {
            ProjectReleaseProperty.each {
                project.getProjectPropertyFromUser(it, null, false, br)
            }
            File propsFile = new File('gradle.props')
            propsFile << project.listPropertiesAsString(ProjectReleaseProperty.class, true)
        }
    }
}
