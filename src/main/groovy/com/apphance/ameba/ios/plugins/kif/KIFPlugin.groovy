package com.apphance.ameba.ios.plugins.kif

import java.io.File;
import java.util.Collection;

import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging

import com.apphance.ameba.ProjectConfiguration;
import com.apphance.ameba.ProjectHelper;
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSXCodeOutputParser;
import com.apphance.ameba.ios.IOSProjectConfiguration;
import com.apphance.ameba.ios.plugins.buildplugin.IOSPlugin

class KIFPlugin implements Plugin<Project> {

    static final String AMEBA_IOS_KIF = 'Ameba iOS KIF'

    static final String IPHONESIMULATOR = 'iphonesimulator'

    Logger logger = Logging.getLogger(KIFPlugin.class)
    Project project
    ProjectHelper projectHelper
    IOSXCodeOutputParser iosConfigurationAndTargetRetriever
    ProjectConfiguration conf
    IOSProjectConfiguration iosConf
    String KIFConfiguration


    void apply(Project project) {
        ProjectHelper.checkAllPluginsAreLoaded(project, this.class, IOSPlugin.class)
        use (PropertyCategory) {
            this.project = project
            this.projectHelper = new ProjectHelper()
            this.iosConfigurationAndTargetRetriever = new IOSXCodeOutputParser()
            this.conf = project.getProjectConfiguration()
            this.iosConf = iosConfigurationAndTargetRetriever.getIosProjectConfiguration(project)
            this.KIFConfiguration = project.readProperty(IOSKifProperty.KIF_CONFIGURATION)
            prepareKIFTemplatesTask()
            prepareBuildKIFReleaseTask()
            prepareRunKIFTestsTask()
            prepareRunSingleKIFTestTask()
            project.prepareSetup.prepareSetupOperations << new PrepareIosKIFSetupOperation()
            project.verifySetup.verifySetupOperations << new VerifyIosKIFSetupOperation()
            project.showSetup.showSetupOperations << new ShowIosKIFSetupOperation()
        }
    }

    Collection<String> findAllTests(String family) {
        def tests = []
        File KIFTests = new File (project.rootDir, "KIFTests/Tests")
        DefaultGroovyMethods.eachFile(KIFTests, {
            if (it.name.endsWith(family + ".m")) {
                logger.lifecycle("Adding test ${it}")
                tests << it.name.split("\\.")[0]
            }
        })
        return tests
    }

    private runSingleKIFTest(String test, String family) {
        logger.lifecycle("Running test ${test} for ${family}")
        File cfFixedDirectory = new File (conf.tmpDirectory, "KIF/${family}/${test}")
        cfFixedDirectory.mkdirs()
        cfFixedDirectory.deleteDir();
        def ant = new AntBuilder()
        ant.unzip(src: getKIFTemplate(family), dest: cfFixedDirectory, overwrite: true)
        File documentsDirectory = new File (cfFixedDirectory, "Documents")
        documentsDirectory.mkdirs();
        File runTestScript = new File(conf.tmpDirectory,"run_test_${family}_${test}.bash")
        String baseScript = """#!/bin/bash
    export CFFIXED_USER_HOME=${cfFixedDirectory}
    """
        String killall = """
    killall "iPhone Simulator"
    """
        baseScript += killall
        File KIFError = new File(conf.tmpDirectory,"KIF_error_${family}_${test}.txt")
        File KIFOutput = new File(conf.tmpDirectory,"KIF_output_${family}_${test}.txt")

        def simulatorSdk = '5.0'
        if (iosConf.simulatorsdk.startsWith(IPHONESIMULATOR) && iosConf.simulatorsdk != IPHONESIMULATOR) {
            simulatorSdk = iosConf.simulatorsdk.substring(IPHONESIMULATOR.length())
        }
        baseScript += """
    echo Running test ${test} in directory ${cfFixedDirectory}
    export TEST_CLASS_NAME=${test}
    iphonesim launch ${project.rootDir}/build/${KIFConfiguration}-iphonesimulator/KIFTests.app ${simulatorSdk} ${family.toLowerCase()}  >${KIFOutput} 2>&1
    cat ${KIFOutput}
    XMLS=`find ${cfFixedDirectory} -name 'TEST-*.xml' | wc -l | sed 's/^[ \\n\\t]*//g' | sed 's/[ \\n\\t]*\$//g'`
    echo Number of xmls: \$XMLS
    if [ "\$XMLS" = "0" ]; then
       RESULT=1
    else
       RESULT=0
    fi
    echo Result: \$RESULT
    """
        baseScript += killall
        baseScript += """
    exit \$RESULT
"""
        runTestScript.text = baseScript
        projectHelper.executeCommand(project, [
            "chmod",
            "a+x",
            "${runTestScript}"
        ])

        File applicationAppFile = new File(project.rootDir, "build/${KIFConfiguration}-iphonesimulator/KIFTests.app")
        projectHelper.executeCommand(project, [
            "/bin/bash",
            "${runTestScript}"
        ],
        true,
        null,
        null,
        4
        )
    }

    File getKIFTemplate(String family) {
        return new File(conf.tmpDirectory, "KIF/${family}/${family}_template.zip")
    }

    private void prepareKIFTemplatesTask() {
        def task = project.task('prepareKIFTemplates')
        task.description = "Prepares templates for foneMonkey directories"
        task.group = AMEBA_IOS_KIF
        task << {
            iosConf.families.each { family ->
                def instream = this.getClass().getResourceAsStream("/com/apphance/ameba/ios/plugins/${family}_workspace.zip")
                File outFile = getKIFTemplate(family)
                outFile.parentFile.mkdirs()
                outFile << instream
            }
        }
        task.dependsOn(project.readProjectConfiguration)
    }


    void prepareBuildKIFReleaseTask() {
        def task = project.task('buildKIFRelease')
        task.description = "Builds KIF release. Requires KIFTests target defined in the project"
        task.group = AMEBA_IOS_KIF
        task << {
            def configuration = "${KIFConfiguration}"
            def target = "KIFTests"
            logger.lifecycle( "\n\n\n=== Building DEBUG target ${target}, configuration ${configuration}  ===")
            projectHelper.executeCommand(project, [
                "xcodebuild" ,
                "-target",
                target,
                "-configuration",
                configuration,
                "-sdk",
                this.iosConf.simulatorsdk
            ])
        }
        task.dependsOn(project.readProjectConfiguration)
        task.dependsOn(project.prepareKIFTemplates)
    }


    private void prepareRunKIFTestsTask() {
        def task = project.task('runKIFTests')
        task.description = "Build and executes KIF tests. Requires KIFTests target. Produces result in tmp directory"
        task.group = AMEBA_IOS_KIF
        task << {
            logger.lifecycle("Finding tests to run for KIF test")
            iosConf.families.each { family ->
                logger.lifecycle("Finding tests to run for ${family}")
                findAllTests(family).each { test ->
                    runSingleKIFTest(test, family)
                }
            }
            logger.lifecycle("Finished processing KIF test")
            project.ant {
                move(todir: conf.tmpDirectory, flatten: true) {
                    fileset(dir: new File(conf.tmpDirectory, 'KIF')) { include(name:'**/TEST*.xml') }
                }
            }
        }
        task.dependsOn(project.buildKIFRelease)
    }


    private void prepareRunSingleKIFTestTask() {
        def task = project.task('runSingleKIFTest')
        task.description = "Executes single test with KIF. Requires KIF.test.family (iPad, iPhone) and KIF.test.name project property"
        task.group = AMEBA_IOS_KIF
        task << {
            use(PropertyCategory) {
                def test = project.readExpectedProperty("KIF.test.name")
                def family = project.readExpectedProperty("KIF.test.family")
                runSingleKIFTest(test, family)
            }
        }
        task.dependsOn(project.buildKIFRelease)
    }
}
