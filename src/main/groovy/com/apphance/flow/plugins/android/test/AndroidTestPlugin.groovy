package com.apphance.flow.plugins.android.test

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidTestConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.plugins.android.buildplugin.tasks.CopySourcesTask
import com.apphance.flow.plugins.android.test.tasks.RunRobolectricTestsTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_TEST
import static org.gradle.api.logging.Logging.getLogger

/**
 * Performs android testing.
 */
class AndroidTestPlugin implements Plugin<Project> {

    private logger = getLogger(getClass())

    @Inject AndroidConfiguration conf
    @Inject AndroidTestConfiguration testConf
    @Inject AndroidVariantsConfiguration variantsConf

    @Override
    void apply(Project project) {
        if (testConf.isEnabled()) {
            logger.lifecycle("Applying plugin ${this.class.simpleName}")

            project.task('testAll', group: FLOW_TEST, description: "Run all android tests")

            variantsConf.variants.each { AndroidVariantConfiguration variantConf ->
                def testTaskName = "test${variantConf.name.capitalize()}"
                project.task(testTaskName,
                        type: RunRobolectricTestsTask,
                        dependsOn: CopySourcesTask.NAME).variantConf = variantConf

                project.testAll.dependsOn testTaskName
            }
        }
    }
}