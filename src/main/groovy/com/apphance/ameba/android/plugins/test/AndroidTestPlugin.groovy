package com.apphance.ameba.android.plugins.test


import java.io.IOException
import java.net.Inet4Address
import java.net.ServerSocket

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectHelper;
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.android.AndroidBuildXmlHelper
import com.apphance.ameba.android.AndroidManifestHelper
import com.apphance.ameba.android.AndroidProjectConfiguration
import com.apphance.ameba.android.AndroidProjectConfigurationRetriever
import com.sun.org.apache.xpath.internal.XPathAPI

/**
 * Performs standard android testing.
 */
class AndroidTestPlugin implements Plugin<Project>{
    static Logger logger = Logging.getLogger(AndroidTestPlugin.class)

    private static final String TEST_RUNNER = "pl.polidea.instrumentation.PolideaInstrumentationTestRunner"
    private static final String AVD_PATH = 'avds'
    private static final int MAX_EMULATOR_STARTUP_TIME = 360 * 1000

    ProjectHelper projectHelper
    AndroidProjectConfigurationRetriever androidConfRetriever
    AndroidProjectConfiguration androidConf
    File androidTestDirectory
    AndroidManifestHelper androidManifestHelper
    def testProjectManifest
    String emmaDumpFile
    String xmlJUnitDir
    File coverageDir
    File rawDir
    File coverageEmFile
    File coverageEcFile
    File adbBinary
    File avdDir
    AndroidBuildXmlHelper buildXmlHelper

    String emulatorName
    String emulatorTargetName

    String testProjectPackage
    String testProjectName
    String emulatorSkin
    String emulatorCardSize
    boolean emulatorSnapshotsEnabled
    boolean useEmma
    boolean testPerPackage
    boolean emulatorNoWindow

    int emulatorPort
    Process emulatorProcess
    Process logcatProcess

    public void apply(Project project) {
        this.projectHelper = new ProjectHelper()
        this.androidConfRetriever = new AndroidProjectConfigurationRetriever()
        this.androidConf = androidConfRetriever.getAndroidProjectConfiguration(project)
        this.androidManifestHelper = new AndroidManifestHelper()
        this.buildXmlHelper = new AndroidBuildXmlHelper()
        readConfiguration(project)
        prepareEmmaConfiguration(project)
        prepareCreateAvdTask(project)
        prepareAndroidTestingTask(project)
        prepareCleanAvdTask(project)
        prepareStartEmulatorTask(project)
        prepareStopAllEmulatorsTask(project)
    }

    private void readConfiguration(Project project) {
        use(PropertyCategory) {
            androidTestDirectory = new File(project.rootDir,project.readProperty(AndroidTestProperty.TEST_DIRECTORY))
            rawDir = new File(project.rootDir, 'res/raw')
            testProjectManifest = androidManifestHelper.getParsedManifest(androidTestDirectory)
            testProjectPackage = XPathAPI.selectSingleNode(testProjectManifest, "/manifest/@package").nodeValue
            testProjectName = buildXmlHelper.readProjectName(androidTestDirectory)
            emulatorName = new File('.').getAbsolutePath().replaceAll('[\\\\ /]','_')
            emulatorTargetName = project.readProperty(AndroidTestProperty.EMULATOR_TARGET)
            if (emulatorTargetName == null || emulatorTargetName.empty) {
                emulatorTargetName = androidConf.targetName
            }
            emulatorSkin = project.readProperty(AndroidTestProperty.EMULATOR_SKIN)
            emulatorCardSize = project.readProperty(AndroidTestProperty.EMULATOR_CARDSIZE)
            emulatorSnapshotsEnabled = project.readProperty(AndroidTestProperty.EMULATOR_SNAPSHOT_ENABLED).toBoolean()
            useEmma =  project.readProperty(AndroidTestProperty.USE_EMMA).toBoolean()
            testPerPackage = project.readProperty(AndroidTestProperty.TEST_PER_PACKAGE).toBoolean()
            emulatorNoWindow project.readProperty(AndroidTestProperty.EMULATOR_NO_WINDOW).toBoolean()
        }
    }

    static int findFreeEmulatorPort() {
        int START_PORT = 5554
        int END_PORT = 5584
        for (int port = START_PORT; port<= END_PORT; port+=2) {
            logger.lifecycle("Android emulator probing. trying ports: ${port} ${port+1}")
            try {
                ServerSocket ss1 = new ServerSocket(port, 0, Inet4Address.getByAddress([127, 0, 0, 1]as byte[]))
                try {
                    ss1.setReuseAddress(true)
                    ServerSocket ss2 = new ServerSocket(port+1, 0, Inet4Address.getByAddress([127, 0, 0, 1]as byte[]))
                    try {
                        ss2.setReuseAddress(true)
                        logger.lifecycle("Success! ${port} ${port+1} are free")
                        return port
                    } finally {
                        ss2.close()
                    }
                } finally {
                    ss1.close()
                }
            } catch (IOException e) {
                logger.lifecycle("Could not obtain ports ${port} ${port+1}")
            }
        }
        throw new GradleException("Could not find free emulator port (tried all from ${START_PORT} to ${END_PORT}!... ")
    }

    void prepareEmmaConfiguration(Project project) {
        project.configurations.add('emma')
        project.dependencies.add('emma',project.files([
            new File(androidConf.sdkDirectory,'tools/lib/emma.jar')
        ]))
        project.dependencies.add('emma',project.files([
            new File(androidConf.sdkDirectory,'tools/lib/emma_ant.jar')
        ]))
        emmaDumpFile = "/data/data/${androidConf.mainProjectPackage}/coverage.ec"
        xmlJUnitDir = "/data/data/${androidConf.mainProjectPackage}/files/"
        coverageDir = new File(project.rootDir,'tmp/coverage')
        coverageEmFile = new File(coverageDir,'coverage.em')
        coverageEcFile = new File(coverageDir,'coverage.ec')
        adbBinary = new File(androidConf.sdkDirectory,'platform-tools/adb')
        avdDir = new File(project.rootDir,AVD_PATH)
    }

    private void prepareCleanAvdTask(Project project) {
        def task = project.task('cleanAVD')
        task.description = "Cleans AVDs for emulators"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_TEST
        task << {
            project.ant.delete(dir: avdDir)
        }
    }

    private void prepareStopAllEmulatorsTask(Project project) {
        def task = project.task('stopAllEmulators')
        task.description = "Stops all emulators and accompanying logcat (includes stopping adb)"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_TEST
        task << {
            projectHelper.executeCommand(project,project.rootDir, ['killall', 'emulator-arm'],false)
            projectHelper.executeCommand(project,project.rootDir, ['killall', 'adb'], false)
        }
    }


    void prepareCreateAvdTask(Project project) {
        def task = project.task('createAVD')
        task.description = "prepares AVDs for emulator"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_TEST
        task << {
            use (PropertyCategory) {
                boolean emulatorExists = projectHelper.executeCommand(
                        project,
                        project.rootDir,[
                            'android',
                            'list',
                            'avd',
                            '-c'
                        ]).any { it == emulatorName }
                if (!avdDir.exists() || !emulatorExists) {
                    avdDir.mkdirs()
                    logger.lifecycle("Creating emulator avd: ${emulatorName}")
                    def avdCreateCommand =  [
                        'android',
                        '-v',
                        'create',
                        'avd',
                        '-n',
                        emulatorName,
                        '-t',
                        emulatorTargetName,
                        '-s',
                        emulatorSkin,
                        '-c',
                        emulatorCardSize,
                        '-p',
                        avdDir,
                        '-f'
                    ]
                    if (emulatorSnapshotsEnabled) {
                        avdCreateCommand << '-a'
                    }
                    projectHelper.executeCommand(project,project.rootDir, avdCreateCommand as String[], false, null, ['no'])
                    logger.lifecycle("Created emulator avd: ${emulatorName}")
                } else {
                    logger.lifecycle("Skipping creating emulator: ${emulatorName}. It already exists.")
                }
            }
        }
        task.dependsOn(project.readAndroidProjectConfiguration)
    }

    private void prepareAndroidTestingTask(Project project) {
        def task = project.task('testAndroid')
        task.description = "Runs android tests on the project"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_TEST
        task << {
            prepareTestBuilds(project)
            startEmulator(project, emulatorNoWindow)
            try {
                installTestBuilds(project)
                runAndroidTests(project)
            } finally {
                stopEmulator(project)
            }
        }
        task.dependsOn(project.createAVD)
    }

    private boolean deleteNonEmptyDirectory(File path) {
        if (path.isDirectory()) {
            String[] children = path.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteNonEmptyDirectory(new File(path, children[i]));
                if (!success) {
                    return false
                }
            }
        }
        path.delete()
        return true
    }

    private prepareTestBuilds(Project project) {
        coverageDir.delete()
        coverageDir.mkdirs()
        coverageDir.mkdir()
        // empty raw dir first
        if (rawDir.exists()) {
            deleteNonEmptyDirectory(rawDir)
        }
        rawDir.mkdir()
        String [] commandAndroid = [
            "android",
            "update",
            "test-project",
            "-p",
            ".",
            "-m",
            "../../"
        ]
        projectHelper.executeCommand(project, androidTestDirectory,commandAndroid)
        String [] commandAnt = [
            "ant",
            "clean"
        ]
        projectHelper.executeCommand(project, androidTestDirectory,commandAnt)
        projectHelper.executeCommand(project,project.rootDir,commandAnt)
        String [] commandAntTest = [
            "ant",
            "emma",
            "clean",
            "instrument",
            "-Dtest.runner=${TEST_RUNNER}"
        ]
        projectHelper.executeCommand(project, androidTestDirectory,commandAntTest)
        File localEmFile  = new File(androidTestDirectory,'coverage.em')
        if (localEmFile.exists()) {
            boolean res = localEmFile.renameTo(coverageEmFile)
            logger.lifecycle("Renamed ${localEmFile} to ${coverageEmFile} with result: ${res}")
        } else {
            logger.lifecycle("No ${localEmFile}. Not renaming to ${coverageEmFile}")
        }
    }

    private void installTestBuilds(Project project) {
        projectHelper.executeCommand(project, androidTestDirectory,[
            adbBinary,
            '-s',
            "emulator-${emulatorPort}",
            'uninstall',
            androidConf.mainProjectPackage
        ], false)
        projectHelper.executeCommand(project, androidTestDirectory,[
            adbBinary,
            '-s',
            "emulator-${emulatorPort}",
            'uninstall',
            testProjectPackage
        ], false)
        projectHelper.executeCommand(project,project.rootDir,[
            adbBinary,
            '-s',
            "emulator-${emulatorPort}",
            'install',
            "bin/${androidConf.mainProjectName}-instrumented.apk"
        ])
        projectHelper.executeCommand(project, androidTestDirectory,[
            adbBinary,
            '-s',
            "emulator-${emulatorPort}",
            'install',
            "bin/${testProjectName}-instrumented.apk"
        ])
    }

    void runLogCat(Project project) {
        logger.lifecycle("Starting logcat monitor on ${emulatorName}")
        String [] commandRunLogcat =[
            adbBinary,
            '-s',
            "emulator-${emulatorPort}",
            'logcat',
            '-v',
            'time'
        ]
        def outFile = new File(project.rootDir,"tmp/logcat.txt")
        logcatProcess = projectHelper.executeCommandInBackground(project.rootDir, outFile, commandRunLogcat)
    }

    void waitUntilEmulatorReady(Project project) {
        logger.lifecycle("Waiting until emulator is ready ${emulatorName}")
        String [] commandRunShell =[
            adbBinary,
            '-s',
            "emulator-${emulatorPort}",
            'shell',
            'getprop',
            'dev.bootcomplete'
        ]
        def startTime = System.currentTimeMillis()
        while(true) {
            def res = projectHelper.executeCommand(project,project.rootDir, commandRunShell, false)
            if (res !=  null && res[0] == "1") {
                logger.lifecycle("Emulator is ready ${emulatorName}!")
                break
            }
            if (System.currentTimeMillis() - startTime > MAX_EMULATOR_STARTUP_TIME) {
                emulatorProcess?.destroy()
                throw new GradleException("Could not start emulator in  ${MAX_EMULATOR_STARTUP_TIME/1000} s. Giving up.")
            }
            Thread.sleep(4000) // wait 4 seconds between retries
        }
    }


    void runAndroidTests(Project project) {
        logger.lifecycle("Running tests on ${emulatorName}")
        if (testPerPackage) {
            def allPackages = []
            projectHelper.findAllPackages("", new File(androidTestDirectory,"src"), allPackages)
            logger.lifecycle("Running tests on packages ${allPackages}")
            allPackages.each {
                logger.lifecycle("Running tests for package ${it}")
                def commandRunTests = prepareTestCommandLine(it)
                logger.lifecycle("Running  ${commandRunTests}")
                projectHelper.executeCommand(project, androidTestDirectory, commandRunTests)
            }
        } else {
            def commandRunTests = prepareTestCommandLine(null)
            projectHelper.executeCommand(project, androidTestDirectory, commandRunTests)
        }
        if (useEmma) {
            extractEmmaCoverage(project)
            prepareCoverageReport(project)
        }
        extractXMLJUnitFiles(project)
    }

    String [] prepareTestCommandLine(String packageName) {
        def commandLine = [
            adbBinary,
            '-s',
            "emulator-${emulatorPort}",
            'shell',
            'am',
            'instrument',
            '-w'
        ]
        if (packageName != null) {
            commandLine += [
                '-e',
                'package',
                packageName
            ]
        }
        if (useEmma) {
            commandLine += [
                '-e',
                'coverage',
                'true',
                '-e',
                'coverageFile',
                emmaDumpFile,
            ]
        }
        commandLine<< "${testProjectPackage}/${TEST_RUNNER}"
        return (String[]) commandLine
    }

    void extractEmmaCoverage(Project project) {
        logger.lifecycle("Extracting coverage report from ${emulatorName}")
        String [] commandDownloadCoverageFile =[
            adbBinary,
            '-s',
            "emulator-${emulatorPort}",
            'pull',
            emmaDumpFile,
            coverageEcFile
        ]
        projectHelper.executeCommand(project, androidTestDirectory, commandDownloadCoverageFile)
        logger.lifecycle("Pulled coverage report from ${emulatorName} to ${coverageDir}...")
    }

    void extractXMLJUnitFiles(Project project) {
        logger.lifecycle("Extracting coverage report from ${emulatorName}")
        String [] commandDownloadXmlFile =[
            adbBinary,
            '-s',
            "emulator-${emulatorPort}",
            'pull',
            xmlJUnitDir
        ]
        projectHelper.executeCommand(project, coverageDir, commandDownloadXmlFile)
    }

    void prepareCoverageReport(Project project) {
        project.ant.taskdef( resource:"emma_ant.properties",
                classpath: project.configurations.emma.asPath)
        project.ant.emma {
            report(sourcepath : "${project.rootDir}/src") {
                fileset(dir : coverageDir) {
                    include(name : 'coverage.ec*')
                    include(name : 'coverage.em')
                }
                xml(outfile : "${coverageDir}/android_coverage.xml")
                txt(outfile : "${coverageDir}/android_coverage.txt")
                html(outfile : "${coverageDir}/android_coverage.html")
            }
        }
        logger.lifecycle("Prepared coverage report from ${emulatorName} to ${coverageDir}...")
    }

    void startEmulator(Project project, boolean noWindow) {
        logger.lifecycle("Starting emulator ${emulatorName}")
        emulatorPort = findFreeEmulatorPort()
        def emulatorCommand = [
            'emulator',
            '-avd',
            emulatorName,
            '-port',
            emulatorPort,
            '-no-boot-anim'
        ]
        if (noWindow) {
            emulatorCommand << '-no-window'
        }
        def outFile = new File(project.rootDir,"tmp/emulator.txt")
        emulatorProcess = projectHelper.executeCommandInBackground(project.rootDir, outFile, emulatorCommand)
        Thread.sleep(4000) // sleep for 4 secs.
        runLogCat(project)
        waitUntilEmulatorReady(project)
        logger.lifecycle("Started emulator ${emulatorName}")
    }

    void stopEmulator(Project project) {
        logger.lifecycle("Stopping emulator ${emulatorName} and logcat")
        emulatorProcess?.destroy()
        logcatProcess?.destroy()
        logger.lifecycle("Stopped emulator ${emulatorName} and logcat")
    }

    private void prepareStartEmulatorTask(Project project) {
        def task = project.task('startEmulator')
        task.description = "Starts emulator for manual inspection"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_TEST
        task << { startEmulator(project, true, false) }
        task.dependsOn(project.readAndroidProjectConfiguration)
    }
}
