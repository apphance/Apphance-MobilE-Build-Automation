package com.apphance.ameba.plugins.project.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_TEST
import static com.apphance.ameba.util.file.FileManager.MAX_RECURSION_LEVEL
import static groovy.io.FileType.FILES

/**
 * Checks if all tests are ok.
 *
 */
//TODO do we need this task?
class CheckTestsTask extends DefaultTask {

    static String NAME = 'checkTests'
    String group = AMEBA_TEST
    String description = 'Checks if there are any failed junit test results in the project and fails if there are'

    @TaskAction
    void checkTests() {
        List<String> failingTests = new ArrayList<String>();
        File testDir = project.rootDir
        testDir.traverse([type: FILES, maxDepth: MAX_RECURSION_LEVEL]) {
            String fileName = it.name
            if (fileName.matches("TEST.*\\.xml")) {
                Node testsuites = new XmlParser().parse(it)
                testsuites.testsuite.each() { testsuite ->
                    if (testsuite.attributes()["failures"] != "0") {
                        failingTests.add(testsuite.attributes()["name"])
                    }
                }
            }
        }
        if (!failingTests.isEmpty()) {
            throw new GradleException("build failed, since following tests failed: " + failingTests.toListString())
        }
    }
}
