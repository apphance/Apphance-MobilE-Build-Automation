package com.apphance.ameba.plugins.android.buildplugin

import com.apphance.ameba.AbstractVerifySetupOperation
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.plugins.android.AndroidProjectConfiguration
import com.apphance.ameba.plugins.android.AndroidProjectConfigurationRetriever
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Verifies if all android properties are correctly setup.
 *
 */
class VerifyAndroidSetupOperation extends AbstractVerifySetupOperation {
    Logger logger = Logging.getLogger(VerifyAndroidSetupOperation.class)

    AndroidProjectConfiguration androidConf

    VerifyAndroidSetupOperation() {
        super(AndroidProjectProperty.class)
    }

    void verifySetup() {
        androidConf = AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration(project)
        use(PropertyCategory) {
            Properties projectProperties = readProperties()
            AndroidProjectProperty.each { checkProperty(projectProperties, it) }
            checkVariant()
            checkMinSdkTarget()
            allPropertiesOK()
        }
    }

    void checkVariant() {
        use(PropertyCategory) {
            String mainVariant = project.readProperty(AndroidProjectProperty.MAIN_VARIANT)
            if (mainVariant != null && !mainVariant.empty && !androidConf.variants.contains(mainVariant)) {
                throw new GradleException("""The main variant in ${AndroidProjectProperty.MAIN_VARIANT.propertyName}: ${mainVariant} can only be one of ${androidConf.variants}""")
            }
        }
    }

    void checkMinSdkTarget() {
        use(PropertyCategory) {
            String target = project.readProperty(AndroidProjectProperty.MIN_SDK_TARGET)
            List targets = androidConf.availableTargets
            if (target != null && !target.empty && !targets.contains(target)) {
                throw new GradleException("""The min sdk target ${AndroidProjectProperty.MIN_SDK_TARGET.propertyName}: ${target} can only be one of ${targets}""")
            }
        }
    }
}
