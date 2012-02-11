package com.apphance.ameba.plugins.projectconfiguration

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AmebaCommonBuildTaskGroups;
import com.apphance.ameba.ProjectConfiguration;
import com.apphance.ameba.ProjectHelper;

import groovy.io.FileType

class PrepareBaseSetupTask extends DefaultTask {

    Logger logger = Logging.getLogger(PrepareBaseSetupTask.class)
    ProjectConfiguration conf

    PrepareBaseSetupTask() {
        this.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        this.description = 'Walks you through the base part of setup of the project.'
        this.conf = new ProjectConfiguration()
        //inject myself as dependency for umbrella prepareSetup
        project.prepareSetup.dependsOn(this)
        //		this.logLevel = LogLevel.QUIET
        this.logging.setLevel(LogLevel.QUIET)
    }

    @TaskAction
    void prepareSetup() {
        System.out.println("""#######################
# Preparing base setup
#######################""")
        def files = []
        new File('.').eachFileRecurse(FileType.FILES) {
            if (it.name.equals('Icon.png') || it.name.equals('icon.png')) {
                def path = it.path
                files << path
            }
        }
        System.out.println('Type values for properties')
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in))

        for (ProjectBaseProperty property : ProjectBaseProperty.values()) {
            if (property == ProjectBaseProperty.PROJECT_ICON_FILE) {
                // will be handled separetly
                continue;
            }
            ProjectHelper.getProjectPropertyFromUser(project, property.propertyName, property.description, null, false, br)
        }
        ProjectHelper.getProjectPropertyFromUser(project, ProjectBaseProperty.PROJECT_ICON_FILE.propertyName, ProjectBaseProperty.PROJECT_ICON_FILE.description, files, true, br)
        File file = new File('gradle.props')
        file.delete()
        file << ProjectBaseProperty.printProperties(project, false)
    }
}
