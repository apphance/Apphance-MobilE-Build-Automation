package com.apphance.ameba.plugins.ios.ocunit.tasks

import com.apphance.ameba.configuration.ios.IOSUnitTestConfiguration
import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.plugins.ios.IOSProjectConfiguration
import com.apphance.ameba.plugins.projectconfiguration.ProjectConfiguration
import com.google.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.ios.ocunit.IOSUnitTestPlugin.AMEBA_IOS_UNIT
import static org.gradle.api.logging.Logging.getLogger

class RunUnitTestsTasks extends DefaultTask{

    private l = getLogger(getClass())

    static String NAME = 'runUnitTests'
    String group = AMEBA_IOS_UNIT
    String description = "Build and executes Unit tests. Requires UnitTests target."

    @Inject
    IOSExecutor iosExecutor

    @Inject
    ProjectConfiguration conf

    @Inject
    IOSProjectConfiguration iosProjectConf

    @Inject
    IOSUnitTestConfiguration iosUnitTestConf

    @TaskAction
    void runUnitTests() {
        def configuration = iosUnitTestConf.configuration.value
        def target = iosUnitTestConf.target.value
        conf.tmpDirectory.mkdirs()
        def testResults = new File(conf.tmpDirectory, "test-${target}-${configuration}.txt")
        l.lifecycle("Trying to create file: ${testResults.canonicalPath}")
        testResults.createNewFile()
        iosExecutor.buildTestTarget(project.rootDir, target, configuration, "${testResults.canonicalPath}".toString())
        OCUnitParser parser = new OCUnitParser()
        parser.parse(testResults.text.split('\n') as List)
        File unitTestFile = new File(conf.tmpDirectory, "TEST-all.xml")
        new XMLJunitExporter(unitTestFile, parser.testSuites).export()
    }
}
