package com.apphance.ameba.ios.plugins.build

import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyManager;
import com.apphance.ameba.ios.IOSProjectProperty;

import groovy.io.FileType

class IOSPrepareSetupTask extends DefaultTask {
    Logger logger = Logging.getLogger(IOSPrepareSetupTask.class)
    ProjectConfiguration conf

    IOSPrepareSetupTask() {
        this.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        this.description = 'Walks you through the iOS part of setup of the project.'
        this.conf = new ProjectConfiguration()
        //inject myself as dependency for umbrella prepareSetup
        project.prepareSetup.dependsOn(this)
    }

    @TaskAction
    void prepareSetup() {
        System.out.println("""#######################
# Preparing iOS setup
#######################""")
        System.out.println('Type values for properties')
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in))

        for (IOSProjectProperty property : IOSProjectProperty.values()) {
            if (property == IOSProjectProperty.PLIST_FILE) {
                // handled separetly
                continue;
            }
            ProjectHelper.getProjectPropertyFromUser(project, property.propertyName, property.getDescription(), null, false, br)
        }
        def files = []
        new File('.').eachFileRecurse(FileType.FILES) {
            if (it.name.endsWith(".plist")) {
                def path = it.path
                files << path
            }
        }
        ProjectHelper.getProjectPropertyFromUser(project, IOSProjectProperty.PLIST_FILE.propertyName, IOSProjectProperty.PLIST_FILE.description, files, true, br)
        File file = new File('gradle.props')
        file << PropertyManager.listPropertiesAsString(project, IOSProjectProperty.class, false, "iOS properties")
    }
}
