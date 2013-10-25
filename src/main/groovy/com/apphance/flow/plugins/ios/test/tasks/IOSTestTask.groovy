package com.apphance.flow.plugins.ios.test.tasks

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.test.tasks.runner.IOSTest5Runner
import com.apphance.flow.plugins.ios.test.tasks.runner.IOSTestLT5Runner
import com.apphance.flow.util.Version
import groovy.transform.PackageScope
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_TEST
import static com.google.common.base.Preconditions.checkArgument
import static org.apache.commons.lang.StringUtils.isNotEmpty

class IOSTestTask extends DefaultTask {

    String group = FLOW_TEST
    String description = 'Builds variant and runs test against it.'

    @Inject IOSExecutor executor
    @Inject IOSTestLT5Runner testLT5Runner
    @Inject IOSTest5Runner test5Runner

    AbstractIOSVariant variant

    private final static Version BORDER_VERSION = new Version('5')

    @TaskAction
    void test() {
        logger.info("Running unit tests with variant: $variant.name")
        if (BORDER_VERSION > xcodeVersion())
            testLT5Runner.runTests(variant)
        else
            test5Runner.runTests(variant)
    }

    @PackageScope
    Version xcodeVersion() {
        def version = executor.xCodeVersion
        checkArgument(isNotEmpty(version))
        new Version(version)
    }
}
