package com.apphance.flow.plugins.ios.apphance

/**
 * This plugin provides automatic source code integration of apphance framework and defines a task for uploading built
 * artifacts (ipa, ahSYM and image montage) to 'apphance.com'. To upload artifacts apphance user and pass must be passed
 * via flow.properties file, system (-D) properties or environment variables.
 * <br/><br/>
 * For more info visit: <a href="http://apphance.com">apphance.com</a>.
 * <br/><br/>
 * No special task is defined for apphance integration. If apphance mode for particular variant is one of 'QA', 'SILENT'
 * or 'PROD' source code will be modified before building the variant.
 * <br/><br/>
 * Automatic source integration does the following:
 * <ul>
 *     <li>adds necessary frameworks to 'project.pbxproj' file</li>
 *     <li>modifies the PCH file to include apphance framework headers</li>
 *     <li>replaces all NSLog invocations with 'APHLog'</li>
 *     <li>adds apphance session initialization when application starts</li>
 * </ul>
 */
import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.plugins.ios.apphance.tasks.IOSApphanceUploadTask
import com.google.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.Project

import static com.apphance.flow.configuration.apphance.ApphanceMode.*
import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static org.gradle.api.logging.Logging.getLogger

class IOSApphancePlugin implements Plugin<Project> {

    private logger = getLogger(getClass())

    @Inject ApphanceConfiguration apphanceConf
    @Inject IOSVariantsConfiguration variantsConf
    @Inject IOSApphanceEnhancerFactory iosApphanceEnhancerFactory

    @Override
    void apply(Project project) {
        if (apphanceConf.enabled) {
            logger.lifecycle("Applying plugin ${getClass().simpleName}")

            variantsConf.variants.each { variant ->

                if (variant.aphMode.value in [QA, PROD, SILENT] && variant.mode.value == DEVICE) {
                    def enhance = { iosApphanceEnhancerFactory.create(variant).enhanceApphance() }

                    project.tasks[variant.archiveTaskName].doFirst(enhance)

                    def uploadTask =
                        project.task(variant.uploadTaskName,
                                type: IOSApphanceUploadTask,
                                dependsOn: variant.archiveTaskName,
                                description: "Uploads IPA, ahSYM and image montage files to 'apphance.com'."
                        ) as IOSApphanceUploadTask
                    uploadTask.variant = variant
                } else {
                    logger.info("Apphance is disabled for variant '$variant.name'")
                }
            }
        }
    }
}
