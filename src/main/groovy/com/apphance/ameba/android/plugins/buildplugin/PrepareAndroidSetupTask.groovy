package com.apphance.ameba.android.plugins.buildplugin


import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction;

import com.apphance.ameba.AbstractPrepareSetupTask
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.android.AndroidCommandParser;
import com.apphance.ameba.android.AndroidProjectConfiguration;
import com.apphance.ameba.android.AndroidProjectConfigurationRetriever;


class PrepareAndroidSetupTask extends AbstractPrepareSetupTask {
    Logger logger = Logging.getLogger(PrepareAndroidSetupTask.class)

    AndroidProjectConfigurationRetriever retriever = new AndroidProjectConfigurationRetriever()
    AndroidProjectConfiguration androidConf

    PrepareAndroidSetupTask() {
        super(AndroidProjectProperty.class)
        this.dependsOn(project.readAndroidProjectConfiguration, project.prepareBaseSetup)
    }

    @TaskAction
    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        androidConf = retriever.getAndroidProjectConfiguration(project)
        use (PropertyCategory) {
            BufferedReader br = getReader()
            AndroidProjectProperty.each {
                switch (it) {
                    case AndroidProjectProperty.MAIN_VARIANT:
                        project.getProjectPropertyFromUser(it, androidConf.variants, br)
                        break;
                    case AndroidProjectProperty.MIN_SDK_TARGET:
                        List targets = AndroidCommandParser.getTargets(project)
                        project.getProjectPropertyFromUser(it, targets, br)
                        break;
                    default:
                        project.getProjectPropertyFromUser(it, null, br)
                }
            }
            appendProperties()
        }
    }
}
