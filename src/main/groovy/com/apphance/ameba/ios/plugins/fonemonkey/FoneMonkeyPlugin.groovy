package com.apphance.ameba.ios.plugins.fonemonkey

import groovy.text.SimpleTemplateEngine

import java.io.File
import java.util.Collection

import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AmebaArtifact
import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSXCodeOutputParser
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.plugins.buildplugin.IOSPlugin

class FoneMonkeyPlugin implements Plugin<Project> {


    static public final String DESCRIPTION ="""
    <div>
    <div>This plugins provides functionality of Fonemonkey integration testing for iOS.</div>
    <div><br></div>
    <div>More description is needed.</div>
    <div><br></div>
    </div>
    """

    static final String IPHONESIMULATOR = 'iphonesimulator'

    Logger logger = Logging.getLogger(FoneMonkeyPlugin.class)
    Project project
    ProjectHelper projectHelper
    IOSXCodeOutputParser iosConfigurationAndTargetRetriever
    ProjectConfiguration conf
    IOSProjectConfiguration iosConf
    String foneMonkeyConfiguration

    void apply(Project project) {
        ProjectHelper.checkAllPluginsAreLoaded(project, this.class, IOSPlugin.class)
        use (PropertyCategory) {
            this.project = project
            this.projectHelper = new ProjectHelper()
            this.iosConfigurationAndTargetRetriever = new IOSXCodeOutputParser()
            this.conf = project.getProjectConfiguration()
            this.iosConf = iosConfigurationAndTargetRetriever.getIosProjectConfiguration(project)
            this.foneMonkeyConfiguration = project.readProperty(IOSFoneMonkeyProperty.FONE_MONKEY_CONFIGURATION)
            prepareFoneMonkeyTemplatesTask()
            prepareBuildFoneMonkeyReleaseTask()
            prepareRunMonkeyTestsTask()
            prepareRunSingleFoneMonkeyTestTask()
            prepareFoneMonkeyReportTask()
            project.prepareSetup.prepareSetupOperations << new PrepareFoneMonkeySetupOperation()
            project.verifySetup.verifySetupOperations << new VerifyFoneMonkeySetupOperation()
            project.showSetup.showSetupOperations << new ShowFoneMonkeySetupOperation()
        }
    }

    Collection<String> findAllTests(String family) {
        def tests = []
        File monkeyTests = new File (project.rootDir, "MonkeyTests")
        DefaultGroovyMethods.eachFile(monkeyTests, {
            if (it.name.endsWith(family + ".m")) {
                logger.lifecycle("Adding test ${it}")
                tests << it.name.split("\\.")[0]
            }
        })
        return tests
    }

    void prepareIndexMonkeyArtifacts() {
        String otaFolderPrefix = "${conf.projectDirectoryName}/${conf.fullVersionString}"
        iosConf.families.each { family ->
            def tests = findAllTests(family)
            iosConf.monkeyTests.put(family, tests)
            iosConf.monkeyTestImages.put(family, new HashMap<String,AmebaArtifact>())
            iosConf.monkeyTestResults.put(family, new HashMap<String,Collection<AmebaArtifact>>())
            AmebaArtifact file = new AmebaArtifact(
                    name: "FoneMonkey test results file for ${conf.projectName}",
                    url : new URL(conf.baseUrl, "${otaFolderPrefix}/fonemonkey_test_results_${family}.html"),
                    location : new File(conf.otaDirectory,"${otaFolderPrefix}/fonemonkey_test_results_${family}.html"))
            iosConf.foneMonkeyTestResultFiles.put(family,file)
        }
    }

    void prepareAllMonkeyArtifacts(){
        prepareIndexMonkeyArtifacts()
        String otaFolderPrefix = "${conf.projectDirectoryName}/${conf.fullVersionString}"
        iosConf.families.each { family ->
            def tests = findAllTests(family)
            tests.each { test ->
                prepareSingleTestMonkeyArtifacts(family,test,otaFolderPrefix)
            }
            def foneMonkeyResult = iosConf.foneMonkeyTestResultFiles[family]
            foneMonkeyResult.location.parentFile.mkdirs()
            foneMonkeyResult.location.delete()
            URL foneMonkeyTestResultTemplate = this.class.getResource("fonemonkey_test_results.html")
            ResourceBundle rb = ResourceBundle.getBundle(\
                this.class.package.name + ".fonemonkey_test_results",
                    conf.locale, this.class.classLoader)
            SimpleTemplateEngine engine = new SimpleTemplateEngine()
            def binding = [
                        baseUrl : foneMonkeyResult.url,
                        currentDate: conf.buildDate,
                        iosConf: iosConf,
                        family: family,
                        conf: conf,
                        rb: rb
                    ]
            def result = engine.createTemplate(foneMonkeyTestResultTemplate).make(binding)
            foneMonkeyResult.location << (result.toString())
            logger.lifecycle("FoneMonkey test result created for ${family}: ${foneMonkeyResult}")
        }
    }

    void prepareSingleTestMonkeyArtifacts(String family, String test, String otaFolderPrefix) {
        File cfFixedDirectory = new File (conf.tmpDirectory, "monkey/${family}/${test}/")
        File targetDirectory = new File(conf.targetDirectory,"monkey/${family}/${test}/")
        targetDirectory.deleteDir()
        targetDirectory.mkdirs()
        def ant = new AntBuilder()
        ant.copy(todir: targetDirectory, flatten : true) {
            fileset(dir : cfFixedDirectory ) {
                include (name : "**/FM_LOG-*.xml")
                include (name: "**/FM_SCREENSHOT-*.png")
            }
        }
        iosConf.monkeyTestImages[family].put(test,new LinkedList<AmebaArtifact>())
        targetDirectory.eachFile { file ->
            if (file.name.endsWith(".xml")) {
                AmebaArtifact testResult = new AmebaArtifact(
                        name : "Test result for ${family} test: ${test}",
                        url : new URL(conf.baseUrl,"${otaFolderPrefix}/monkey/${family}/${test}/${file.name}"),
                        location : new File(conf.otaDirectory,"${otaFolderPrefix}/monkey/${family}/${test}/${file.name}"))
                iosConf.monkeyTestResults[family].put(test,testResult)
            } else if (file.name.endsWith(".png")){
                AmebaArtifact testImage = new AmebaArtifact(
                        name : "Image test result for ${family} test: ${test}",
                        url : new URL(conf.baseUrl,"${otaFolderPrefix}/monkey/${family}/${test}/${file.name}"),
                        location : new File(conf.otaDirectory,"${otaFolderPrefix}/monkey/${family}/${test}/${file.name}"))
                iosConf.monkeyTestImages[family][test] << testImage
            }
        }
    }

    private runSingleFoneMonkeyTest(String test, String family) {
        logger.lifecycle("Running test ${test} for ${family}")
        File cfFixedDirectory = new File (conf.tmpDirectory, "monkey/${family}/${test}")
        cfFixedDirectory.mkdirs()
        cfFixedDirectory.deleteDir();
        def ant = new AntBuilder()
        ant.unzip(src: getFoneMonkeyTemplate(family), dest: cfFixedDirectory, overwrite: true)
        File documentsDirectory = new File (cfFixedDirectory, "Documents")
        documentsDirectory.mkdirs();
        File runTestScript = new File(conf.tmpDirectory,"run_test_${family}_${test}.bash")
        String baseScript = """#!/bin/bash
    export CFFIXED_USER_HOME=${cfFixedDirectory}
    export FM_ENABLE_AUTOEXIT=AUTO
    export FM_ENABLE_XML_REPORT=TRUE
    export FM_ENABLE_SCREENSHOT=ALL
    """
        String killall = """
    killall "iPhone Simulator"
    """
        baseScript += killall
        File foneMonkeyError = new File(conf.tmpDirectory,"fonemonkey_error_${family}_${test}.txt")
        File foneMonkeyOutput = new File(conf.tmpDirectory,"fonemonkey_output_${family}_${test}.txt")

        def simulatorSdk = '5.0'
        if (iosConf.simulatorsdk.startsWith(IPHONESIMULATOR) && iosConf.simulatorsdk != IPHONESIMULATOR) {
            simulatorSdk = iosConf.simulatorsdk.substring(IPHONESIMULATOR.length())
        }
        baseScript += """
    echo Running test ${test} in directory ${cfFixedDirectory}
    export FM_RUN_SINGLE_TEST=${test}
    iphonesim launch ${project.rootDir}/build/${foneMonkeyConfiguration}-iphonesimulator/RunMonkeyTests.app ${simulatorSdk} ${family.toLowerCase()}  >${foneMonkeyOutput} 2>&1
    cat ${foneMonkeyOutput}
    XMLS=`find ${cfFixedDirectory} -name 'FM_LOG*.xml' | wc -l | sed 's/^[ \\n\\t]*//g' | sed 's/[ \\n\\t]*\$//g'`
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

        File applicationAppFile = new File(project.rootDir, "build/${foneMonkeyConfiguration}-iphonesimulator/RunMonkeyTests.app")
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

    File getFoneMonkeyTemplate(String family) {
        return new File(conf.tmpDirectory, "monkey/${family}/${family}_template.zip")
    }

    private void prepareFoneMonkeyTemplatesTask() {
        def task = project.task('prepareFoneMonkeyTemplates')
        task.description = "Prepares templates for foneMonkey directories"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_FONEMONKEY
        task << {
            iosConf.families.each { family ->
                def instream = this.getClass().getResourceAsStream("/com/apphance/ameba/ios/plugins/${family}_workspace.zip")
                File outFile = getFoneMonkeyTemplate(family)
                outFile.parentFile.mkdirs()
                outFile << instream
            }
        }
        task.dependsOn(project.readProjectConfiguration, project.copyGalleryFiles)
    }


    private void prepareFoneMonkeyReportTask() {
        def task = project.task('prepareFoneMonkeyReport')
        task.description = "Prepares report out of FoneMonkey test execution"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_FONEMONKEY
        task << { prepareAllMonkeyArtifacts() }
        task.dependsOn(project.readProjectConfiguration, project.copyGalleryFiles)
    }

    void prepareBuildFoneMonkeyReleaseTask() {
        def task = project.task('buildFoneMonkeyRelease')
        task.description = "Builds fone monkey release. Requires RunMonkeyTests target defined in the project"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_FONEMONKEY
        task << {
            use (PropertyCategory) {
                def configuration = project.readProperty(IOSFoneMonkeyProperty.FONE_MONKEY_CONFIGURATION)
                def target = "RunMonkeyTests"
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
        }
        task.dependsOn(project.prepareFoneMonkeyTemplates, project.readProjectConfiguration)
    }


    private void prepareRunMonkeyTestsTask() {
        def task = project.task('runMonkeyTests')
        task.description = "Build and executes FoneMonkey tests. Requires RunMonkeyTests target. Produces result in tmp directory"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_FONEMONKEY
        task << {
            iosConf.families.each { family ->
                findAllTests(family).each { test ->
                    runSingleFoneMonkeyTest(test, family)
                }
            }
            project.ant {
                move(todir: conf.tmpDirectory, flatten: true) {
                    fileset(dir: new File(conf.tmpDirectory, 'monkey')) { include(name:'**/TEST*.xml') }
                }
            }
        }
        task.dependsOn(project.buildFoneMonkeyRelease)
    }


    private void prepareRunSingleFoneMonkeyTestTask() {
        def task = project.task('runSingleFoneMonkeyTest')
        task.description = "Executes single test with FoneMonkey. Requires fonemonkey.test.family (iPad, iPhone) and fonemonkey.test.name project property"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_FONEMONKEY
        task << {
            use (PropertyCategory) {
                def test = project.readExpectedProperty("fonemonkey.test.name")
                def family = project.readExpectedProperty("fonemonkey.test.family")
                runSingleFoneMonkeyTest(test, family)
            }
        }
        task.dependsOn(project.buildFoneMonkeyRelease)
    }
}
