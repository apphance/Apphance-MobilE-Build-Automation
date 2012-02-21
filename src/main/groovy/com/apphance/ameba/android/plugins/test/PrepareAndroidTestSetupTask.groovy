package com.apphance.ameba.android.plugins.test



import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractPrepareSetupTask
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.android.AndroidCommandParser;


class PrepareAndroidTestSetupTask extends AbstractPrepareSetupTask {
    Logger logger = Logging.getLogger(PrepareAndroidTestSetupTask.class)
    List BOOLEANS=['true', 'false']

    PrepareAndroidTestSetupTask() {
        super(AndroidTestProperty.class)
        this.dependsOn(project.prepareAndroidSetup)
    }

    @TaskAction
    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        use (PropertyCategory) {
            BufferedReader br = getReader()
            AndroidTestProperty.each {
                switch (it) {
                    case AndroidTestProperty.EMULATOR_SNAPSHOT_ENABLED:
                    case AndroidTestProperty.EMULATOR_NO_WINDOW:
                    case AndroidTestProperty.TEST_PER_PACKAGE:
                    case AndroidTestProperty.USE_EMMA:
                        project.getProjectPropertyFromUser(it, BOOLEANS, br)
                        break;
                    case AndroidTestProperty.EMULATOR_TARGET:
                        List targets = AndroidCommandParser.getTargets()
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
