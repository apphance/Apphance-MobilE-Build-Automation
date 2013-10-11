package com.apphance.flow.plugins.ios.test.tasks.runner

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.executor.linker.FileLinker
import com.apphance.flow.plugins.ios.test.tasks.results.exporter.XMLJunitExporter
import com.apphance.flow.plugins.ios.test.tasks.results.parser.OCUnitParser
import com.apphance.flow.plugins.ios.test.tasks.results.parser.OCUnitTestSuite
import groovy.transform.PackageScope

import javax.inject.Inject

import static com.google.common.base.Preconditions.checkState
import static org.gradle.api.logging.Logging.getLogger

abstract class AbstractIOSTestRunner {

    @Inject IOSExecutor executor
    @Inject FileLinker fileLinker
    def logger = getLogger(getClass())

    protected AbstractIOSVariant variant

    void runTests(AbstractIOSVariant variant) {
        this.variant = variant
        logger.info("Running tests with: ${getClass().simpleName} runner")
    }

    @PackageScope
    File newFile(String extension, String target = '') {
        def results = new File(variant.tmpDir, "${filename(target)}.$extension")
        results.delete()
        results.createNewFile()
        results
    }

    @PackageScope
    String filename(String target) {
        "test-$variant.name${target ? "-$target" : ''}"
    }

    @PackageScope
    Collection<OCUnitTestSuite> parseResults(Collection<String> lines) {
        if (lines) {
            OCUnitParser parser = new OCUnitParser()
            parser.parse lines
            return parser.testSuites
        } else
            logger.warn("No test results for variant $variant.name - parsing skipped")
        []
    }

    @PackageScope
    void parseAndExport(Collection<OCUnitTestSuite> testSuites, File outputFile) {
        new XMLJunitExporter(outputFile, testSuites).export()
    }

    @PackageScope
    void verifyTestResults(Collection<OCUnitTestSuite> ocUnitTestSuites, String errorMessage) {
        checkState(ocUnitTestSuites.every { it.failureCount == 0 }, errorMessage)
    }
}