package com.apphance.ameba.plugins.projectconfiguration.tasks

import com.apphance.ameba.PropertyCategory
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.AmebaCommonBuildTaskGroups.AMEBA_TEST
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.READ_PROJECT_CONFIGURATION_TASK_NAME
import static com.apphance.ameba.util.file.FileManager.MAX_RECURSION_LEVEL
import static groovy.io.FileType.FILES

/**
 * Checks if all tests are ok.
 *
 */
class CheckTestsTask extends DefaultTask {

    CheckTestsTask() {
        use(PropertyCategory) {
            this.group = AMEBA_TEST
            this.description = 'Checks if there are any failed junit test results in the project and fails if therea are'
            this.dependsOn(READ_PROJECT_CONFIGURATION_TASK_NAME)
        }
    }

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
