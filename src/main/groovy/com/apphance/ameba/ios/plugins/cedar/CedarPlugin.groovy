package com.apphance.ameba.ios.plugins.cedar

import java.io.File

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging;

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSXCodeOutputParser
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.plugins.buildplugin.IOSPlugin

/**
 * Plugin for running Cedar tests.
 *
 */
class CedarPlugin implements Plugin<Project> {

    static final String AMEBA_IOS_CEDAR = 'Ameba iOS Cedar'

    String IPHONE_SIMULATOR_SDK = "/Platforms/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator4.3.sdk/"
    Logger logger = Logging.getLogger(CedarPlugin.class)
    Project project
    ProjectHelper projectHelper
    ProjectConfiguration conf
    IOSProjectConfiguration iosConf

    void apply(Project project) {
        ProjectHelper.checkAllPluginsAreLoaded(project, this.class, IOSPlugin.class)
        use (PropertyCategory) {
            this.project = project
            this.projectHelper = new ProjectHelper()
            this.conf = project.getProjectConfiguration()
            this.iosConf = IOSXCodeOutputParser.getIosProjectConfiguration(project)
            prepareCedarTemplatesTask()
            prepareBuildCedarReleasesTask()
            prepareRunCedarTasks()
        }
    }

    private void prepareCedarTemplatesTask() {
        def task = project.task('prepareCedarTemplates')
        task.description = "Prepares templates for cedar testing directories"
        task.group = AMEBA_IOS_CEDAR
        task << {
            def family = "iPhone"
            def instream = this.getClass().getResourceAsStream("/com/apphance/ameba/ios/plugins/${family}_workspace.zip")
            File outFile = getCedarTemplate(family)
            outFile.parentFile.mkdirs()
            outFile << instream
            logger.lifecycle("Copied workspace to ${outFile}")
        }
        task.dependsOn(project.readProjectConfiguration)
    }


    void prepareBuildCedarReleasesTask() {
        def task = project.task('buildCedarReleases')
        task.description = "Builds cedar releases. Requires *Specs targets defined in the project"
        task.group = AMEBA_IOS_CEDAR
        task << {
            def configuration = "Debug"
            def targets = iosConf.alltargets.findAll { it.endsWith('Specs')}
            targets.each { target ->
                logger.lifecycle( "\n\n\n=== Building DEBUG target ${target}, configuration ${configuration}  ===")
                projectHelper.executeCommand(project, iosConf.getXCodeBuildExecutionPath(target, configuration) + [
                    "-target",
                    target,
                    "-configuration",
                    configuration,
                    "-sdk",
                    iosConf.simulatorsdk
                ])
            }
        }
        task.dependsOn(project.prepareCedarTemplates)
        task.dependsOn(project.readProjectConfiguration)
    }

    void prepareRunCedarTasks() {
        def task = project.task('runCedarTests')
        task.description = "Executes cedar test. Requires *Specs targets defined in the project"
        task.group = AMEBA_IOS_CEDAR
        task << {
            def targets = iosConf.alltargets.findAll { it.endsWith('Specs')}
            targets.each { target ->
                def family = "iPhone"
                logger.lifecycle("Running cedar tests for ${family}")
                File cfFixedDirectory = new File (conf.tmpDirectory, "cedar/${family}/tests")
                cfFixedDirectory.mkdirs()
                cfFixedDirectory.deleteDir();
                def ant = new AntBuilder()
                ant.unzip(src: getCedarTemplate(family), dest: cfFixedDirectory, overwrite: true)
                File documentsDirectory = new File (cfFixedDirectory, "Documents")
                documentsDirectory.mkdirs();
                File runTestScript = new File(conf.tmpDirectory,"run_cedar_tests_${family}.bash")
                String baseScript = """#!/bin/bash
export DYLD_ROOT_PATH="${IPHONE_SIMULATOR_SDK}"
export IPHONE_SIMULATOR_ROOT="${IPHONE_SIMULATOR_SDK}"
export CFFIXED_USER_HOME=${cfFixedDirectory}
export CEDAR_HEADLESS_SPECS="1"
export CEDAR_REPORTER_CLASS="CDRJenkinsReporter"
                    """
                File cedarOutput = new File(conf.tmpDirectory,"cedar_output_${family}.txt")

                baseScript += """
echo Running cedar tests in directory ${cfFixedDirectory}
${project.rootDir}/build/Debug-iphonesimulator/${target}.app/${target} -RegisterForSystemEvents >${cedarOutput} 2>&1
RESULT=\$?
cat ${cedarOutput}
exit \$RESULT
                    """
                runTestScript.text = baseScript
                projectHelper.executeCommand(project, [
                    "chmod",
                    "a+x",
                    "${runTestScript}"
                ])
                projectHelper.executeCommand(project, [
                    "/bin/bash",
                    "${runTestScript}"
                ])
            }
        }
        task.dependsOn(project.buildCedarReleases)
    }

    File getCedarTemplate(String family) {
        return new File(conf.tmpDirectory, "cedar/${family}/${family}_template.zip")
    }

    static public final String DESCRIPTION =
"""This plugins provides functionality of Cedar integration testing for iOS.

It executes all tests which are build using Cedar test framework.

More description needed ....

"""



}
