package com.apphance.ameba.plugins.projectconfiguration

import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractPrepareSetupTask;
import com.apphance.ameba.PropertyCategory;

import groovy.io.FileType

class PrepareBaseSetupTask extends AbstractPrepareSetupTask<ProjectBaseProperty> {
    Logger logger = Logging.getLogger(PrepareBaseSetupTask.class)

    PrepareBaseSetupTask() {
        super(ProjectBaseProperty.class)
    }

    @TaskAction
    void prepareSetup() {
        logger.lifecycle("Preparing base setup")
        def files = []
        new File('.').eachFileRecurse(FileType.FILES) {
            if (it.name.equals('Icon.png') || it.name.equals('icon.png')) {
                def path = it.path
                files << path
            }
        }
        System.out.println('Type values for properties')
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in))
        use(PropertyCategory) {
            ProjectBaseProperty.each {
                if (it == ProjectBaseProperty.PROJECT_ICON_FILE) {
                    project.getProjectPropertyFromUser(it, files, true, br)
                } else {
                    project.getProjectPropertyFromUser(it, null, false, br)
                }
            }
            File file = new File('gradle.props')
            file.delete()
            file << project.listPropertiesAsString(ProjectBaseProperty.class, false)
        }
    }
}
