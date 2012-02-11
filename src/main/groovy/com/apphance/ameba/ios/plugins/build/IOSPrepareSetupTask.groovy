package com.apphance.ameba.ios.plugins.build

import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.PropertyCategory;
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
        logger.lifecycle("Preparing iOS setup")
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in))
        use (PropertyCategory) {
            def files = []
            new File('.').eachFileRecurse(FileType.FILES) {
                if (it.name.endsWith(".plist")) {
                    def path = it.path
                    files << path
                }
            }
            IOSProjectProperty.each {
                if (property == IOSProjectProperty.PLIST_FILE) {
                    project.getProjectPropertyFromUser(property, files, true, br)
                } else {
                    project.getProjectPropertyFromUser(property, null, false, br)
                }
            }
            File file = new File('gradle.props')

            file << project.listPropertiesAsString(IOSProjectProperty.class, false)
        }
    }
}
