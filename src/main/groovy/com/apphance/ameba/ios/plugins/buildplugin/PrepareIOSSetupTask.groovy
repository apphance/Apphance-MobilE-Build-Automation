package com.apphance.ameba.ios.plugins.buildplugin

import groovy.io.FileType;

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction;

import com.apphance.ameba.AbstractPrepareSetupTask
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.PropertyCategory


class PrepareIOSSetupTask extends AbstractPrepareSetupTask {
    Logger logger = Logging.getLogger(PrepareIOSSetupTask.class)
    ProjectConfiguration conf

    PrepareIOSSetupTask() {
        super(IOSProjectProperty.class)
    }

    @TaskAction
    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in))
        use (PropertyCategory) {
            def files = []
            new File('.').eachFileRecurse(FileType.FILES) {
                if (it.name.endsWith(".plist")) {
                    def path = it.path.startsWith("./") ? it.path.substring(2) : it.path
                    files << path
                }
            }
            IOSProjectProperty.each {
                if (it == IOSProjectProperty.PLIST_FILE) {
                    project.getProjectPropertyFromUser(it, files, true, br)
                } else {
                    project.getProjectPropertyFromUser(it, null, false, br)
                }
            }
            appendToGeneratedPropertyString(project.listPropertiesAsString(IOSProjectProperty.class, false))
        }
    }
}
