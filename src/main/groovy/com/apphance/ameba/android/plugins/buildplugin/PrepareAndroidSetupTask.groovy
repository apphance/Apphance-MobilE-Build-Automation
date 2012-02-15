package com.apphance.ameba.android.plugins.buildplugin

import groovy.io.FileType;

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction;

import com.apphance.ameba.AbstractPrepareSetupTask
import com.apphance.ameba.PropertyCategory


class PrepareAndroidSetupTask extends AbstractPrepareSetupTask {
    Logger logger = Logging.getLogger(PrepareAndroidSetupTask.class)

    PrepareAndroidSetupTask() {
        super(AndroidProjectProperty.class)
    }

    @TaskAction
    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        def plistFiles = getPlistFiles()
        use (PropertyCategory) {
            BufferedReader br = getReader()
            AndroidProjectProperty.each {
                switch (it) {
                    default:
                        project.getProjectPropertyFromUser(it, null, br)
                }
            }
            appendProperties()
        }
    }

    private List getPlistFiles() {
        def plistFiles = []
        new File('.').eachFileRecurse(FileType.FILES) {
            if (it.name.endsWith(".plist")) {
                def path = it.path.startsWith("./") ? it.path.substring(2) : it.path
                plistFiles << path
            }
        }
        return plistFiles
    }
}
