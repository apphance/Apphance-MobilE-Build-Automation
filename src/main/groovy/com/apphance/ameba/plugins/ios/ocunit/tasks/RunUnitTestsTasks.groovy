package com.apphance.ameba.plugins.ios.ocunit.tasks

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.ios.IOSUnitTestConfiguration
import com.apphance.ameba.executor.IOSExecutor
import com.google.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.ios.ocunit.IOSUnitTestPlugin.AMEBA_IOS_UNIT
import static org.gradle.api.logging.Logging.getLogger

class RunUnitTestsTasks extends DefaultTask {

    private l = getLogger(getClass())

    static String NAME = 'runUnitTests'
    String group = AMEBA_IOS_UNIT
    String description = "Build and executes Unit tests. Requires UnitTests target."

    @Inject
    IOSExecutor iosExecutor
    @Inject
    IOSConfiguration conf
    @Inject
    IOSUnitTestConfiguration unitTestConf

    @TaskAction
    void runUnitTests() {
        def configuration = unitTestConf.configuration.value
        def target = unitTestConf.target.value
        conf.tmpDir.mkdirs()
        def testResults = new File(conf.tmpDir, "test-${target}-${configuration}.txt")
        l.lifecycle("Trying to create file: ${testResults.canonicalPath}")
        testResults.createNewFile()
        iosExecutor.buildTestTarget(project.rootDir, target, configuration, "${testResults.canonicalPath}".toString())
        OCUnitParser parser = new OCUnitParser()
        parser.parse(testResults.text.split('\n') as List)
        File unitTestFile = new File(conf.tmpDir, "TEST-all.xml")
        new XMLJunitExporter(unitTestFile, parser.testSuites).export()
    }
}
