package com.apphance.ameba.android.plugins.buildplugin

import com.apphance.ameba.AbstractPrepareSetupOperation
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.android.AndroidProjectConfigurationRetriever
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Sets up android properties.
 *
 */
class PrepareAndroidSetupOperation extends AbstractPrepareSetupOperation {
    Logger logger = Logging.getLogger(PrepareAndroidSetupOperation.class)

    PrepareAndroidSetupOperation() {
        super(AndroidProjectProperty.class)
    }

    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        def androidConf = AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration(project)
        use(PropertyCategory) {
            BufferedReader br = getReader()
            AndroidProjectProperty.each {
                switch (it) {
                    case AndroidProjectProperty.MAIN_VARIANT:
                        if (!project.hasProperty(it.propertyName) && !androidConf.variants.empty) {
                            project[it.propertyName] = androidConf.variants[0]
                        }
                        project.getProjectPropertyFromUser(it, androidConf.variants, br)
                        break;
                    case AndroidProjectProperty.MIN_SDK_TARGET:
                        if (!project.hasProperty(it.propertyName)) {
                            project[it.propertyName] = androidConf.minSdkTargetName
                        }
                        project.getProjectPropertyFromUser(it, androidConf.availableTargets, br)
                        break;
                    default:
                        project.getProjectPropertyFromUser(it, null, br)
                }
            }
            appendProperties()
        }
    }
}
