package com.apphance.ameba.plugins.android.test.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidTestConfiguration
import com.apphance.ameba.executor.AntExecutor
import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.android.AndroidManifestHelper
import com.apphance.ameba.util.file.FileManager
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.PropertyCategory.readProperty
import static com.apphance.ameba.executor.AntExecutor.CLEAN
import static com.apphance.ameba.executor.AntExecutor.INSTRUMENT
import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_TEST
import static org.gradle.api.logging.Logging.getLogger

class TestAndroidTask extends DefaultTask {

    public static final String TEST_RUNNER = "pl.polidea.instrumentation.PolideaInstrumentationTestRunner"

    private l = getLogger(getClass())

    static String NAME = 'testAndroid'
    String group = AMEBA_TEST
    String description = 'Runs android tests on the project'

    @Inject
    private AndroidConfiguration androidConf
    @Inject
    private AndroidTestConfiguration testConf
    @Inject
    private CommandExecutor executor
    @Inject
    private AntExecutor antExecutor
    @Inject
    private AndroidManifestHelper manifestHelper
    private Process emulatorProcess
    private Process logcatProcess

    @TaskAction
    void testAndroid() {
        // TODO: what to do when no test directory exists ?? should I automatically make one ??
        if (!testConf.testDir.value?.exists()) {
            println "Test directory not found. Please run gradle prepareRobotium in order to create simple Robotium project. Aborting"
            return
        }
        prepareTestBuilds()
        startEmulator(testConf.emulatorNoWindow.value)
        try {
            installTestBuilds()
            runAndroidTests()
        } finally {
            stopEmulator()
        }
    }

    private prepareTestBuilds() {
        testConf.coverageDir.delete()
        testConf.coverageDir.mkdirs()
        testConf.coverageDir.mkdir()
        // empty raw dir first
        if (testConf.rawDir.exists()) {
            deleteNonEmptyDirectory(testConf.rawDir)
        }
        testConf.rawDir.mkdir()
        def commandAndroid = [
                'android',
                'update',
                'test-project',
                '-p',
                '.',
                '-m',
                '../../'
        ]
        executor.executeCommand(new Command(runDir: testConf.testDir.value, cmd: commandAndroid))
        boolean useMockLocation = readProperty(project, testConf.mockLocation.value).toString().toBoolean()
        if (useMockLocation) {
            manifestHelper.addPermissions(project.rootDir, 'android.permission.ACCESS_MOCK_LOCATION')
        }
        try {
            antExecutor.executeTarget testConf.testDir.value, CLEAN, ['test.runner': TEST_RUNNER]
            antExecutor.executeTarget testConf.testDir.value, INSTRUMENT, ['test.runner': TEST_RUNNER]
            File localEmFile = new File(testConf.testDir.value, 'coverage.em')
            if (localEmFile.exists()) {
                boolean res = localEmFile.renameTo(testConf.coverageEMFile)
                l.lifecycle("Renamed ${localEmFile} to ${testConf.coverageEMFile} with result: ${res}")
            } else {
                l.lifecycle("No ${localEmFile}. Not renaming to ${testConf.coverageEMFile}")
            }
        } finally {
            if (useMockLocation) {
                manifestHelper.restoreOriginalManifest(project.rootDir)
            }
        }
    }

    private boolean deleteNonEmptyDirectory(File path) {
        if (path.isDirectory()) {
            String[] children = path.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteNonEmptyDirectory(new File(path, children[i]));
                if (!success) {
                    return false
                }
            }
        }
        path.delete()
        return true
    }

    private void startEmulator(boolean noWindow) {
        l.lifecycle("Starting emulator ${testConf.emulatorName}")
        def emulatorCommand = [
                'emulator',
                '-avd',
                testConf.emulatorName,
                '-port',
                testConf.emulatorPort,
                '-no-boot-anim'
        ]
        if (noWindow) {
            emulatorCommand << '-no-window'
        }
        emulatorProcess = executor.startCommand(new Command(runDir: project.rootDir, cmd: emulatorCommand))
        Thread.sleep(4 * 1000) // sleep for some time.
        runLogCat(project)
        waitUntilEmulatorReady()
        l.lifecycle("Started emulator ${testConf.emulatorName}")
    }

    private void runLogCat(Project project) {
        l.lifecycle("Starting logcat monitor on ${testConf.emulatorName}")
        String[] commandRunLogcat = [
                testConf.getADBBinary(),
                '-s',
                "emulator-${testConf.emulatorPort}",
                'logcat',
                '-v',
                'time'
        ]
        logcatProcess = executor.startCommand(new Command(runDir: project.rootDir, cmd: commandRunLogcat))
    }

    private void waitUntilEmulatorReady() {
        l.lifecycle("Waiting until emulator is ready ${testConf.emulatorName}")
        String[] commandRunShell = [
                testConf.getADBBinary(),
                '-s',
                "emulator-${testConf.emulatorPort}",
                'shell',
                'getprop',
                'dev.bootcomplete'
        ]
        def startTime = System.currentTimeMillis()
        while (true) {
            def res = executor.executeCommand(new Command(runDir: project.rootDir, cmd: commandRunShell, failOnError: false))
            if (res != null && res[0] == "1") {
                l.lifecycle("Emulator is ready ${testConf.emulatorName}!")
                break
            }
            if (System.currentTimeMillis() - startTime > 360 * 1000) {
                emulatorProcess?.destroy()
                throw new GradleException("Could not start emulator in  ${360 * 1000 / 1000.0} s. Giving up.")
            }
            Thread.sleep(4 * 1000)
        }
    }

    private void installTestBuilds() {
        executor.executeCommand(new Command(runDir: testConf.testDir.value, cmd: [
                testConf.getADBBinary(),
                '-s',
                "emulator-${testConf.emulatorPort}",
                'uninstall',
                androidConf.mainPackage.value
        ], failOnError: false))
        executor.executeCommand(new Command(runDir: testConf.testDir.value, cmd: [
                testConf.getADBBinary(),
                '-s',
                "emulator-${testConf.emulatorPort}",
                'uninstall',
                testConf.testProjectPackage
        ], failOnError: false))
        executor.executeCommand(new Command(runDir: project.rootDir, cmd: [
                testConf.getADBBinary(),
                '-s',
                "emulator-${testConf.emulatorPort}",
                'install',
                "bin/${androidConf.projectName.value}-debug.apk"
        ]))
        executor.executeCommand(new Command(runDir: testConf.testDir.value, cmd: [
                testConf.getADBBinary(),
                '-s',
                "emulator-${testConf.emulatorPort}",
                'install',
                "bin/${testConf.testProjectName}-instrumented.apk"
        ]))
    }

    private void runAndroidTests() {
        l.lifecycle("Running tests on ${testConf.emulatorName}")
        if (testConf.testPerPackage) {
            def allPackages = []
            FileManager.findAllPackages("", new File(testConf.getTestDir().value, "src"), allPackages)
            l.lifecycle("Running tests on packages ${allPackages}")
            allPackages.each {
                l.lifecycle("Running tests for package ${it}")
                def commandRunTests = prepareTestCommandLine(it)
                l.lifecycle("Running  ${commandRunTests}")
                executor.executeCommand(new Command(runDir: testConf.getTestDir().value, cmd: commandRunTests))
            }
        } else {
            def commandRunTests = prepareTestCommandLine(null)
            executor.executeCommand(new Command(runDir: testConf.getTestDir().value, cmd: commandRunTests))
        }
        if (testConf.emmaEnabled.value) {
            extractEmmaCoverage()
            prepareCoverageReport()
        }
        extractXMLJUnitFiles()
    }

    private String[] prepareTestCommandLine(String packageName) {
        def commandLine = [
                testConf.getADBBinary(),
                '-s',
                "emulator-${testConf.emulatorPort}",
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
        if (testConf.emmaEnabled.value) {
            commandLine += [
                    '-e',
                    'coverage',
                    'true',
                    '-e',
                    'coverageFile',
                    testConf.emmaDumpFilePath,
            ]
        }
        commandLine << "${testConf.testProjectPackage}/${TEST_RUNNER}"
        return (String[]) commandLine
    }

    private void extractEmmaCoverage() {
        l.lifecycle("Extracting coverage report from ${testConf.emulatorName}")
        String[] commandDownloadCoverageFile = [
                testConf.getADBBinary(),
                '-s',
                "emulator-${testConf.emulatorPort}",
                'pull',
                testConf.emmaDumpFilePath,
                testConf.coverageECFile
        ]
        executor.executeCommand(new Command(runDir: testConf.testDir.value, cmd: commandDownloadCoverageFile))
        l.lifecycle("Pulled coverage report from ${testConf.emulatorName} to ${testConf.coverageDir}...")
    }

    private void prepareCoverageReport() {
        project.ant.taskdef(resource: "emma_ant.properties",
                classpath: project.configurations.emma.asPath)
        project.ant.emma {
            report(sourcepath: "${project.rootDir}/src") {
                fileset(dir: testConf.coverageDir) {
                    include(name: 'coverage.ec*')
                    include(name: 'coverage.em')
                }
                xml(outfile: "${testConf.coverageDir}/android_coverage.xml")
                txt(outfile: "${testConf.coverageDir}/android_coverage.txt")
                html(outfile: "${testConf.coverageDir}/android_coverage.html")
            }
        }
        l.lifecycle("Prepared coverage report from ${testConf.emulatorName} to ${testConf.coverageDir}...")
    }

    private void extractXMLJUnitFiles() {
        l.lifecycle("Extracting coverage report from ${testConf.emulatorName}")
        String[] commandDownloadXmlFile = [
                testConf.getADBBinary(),
                '-s',
                "emulator-${testConf.emulatorPort}",
                'pull',
                testConf.XMLJUnitDirPath
        ]
        executor.executeCommand(new Command(rawDir: testConf.coverageDir, cmd: commandDownloadXmlFile))
    }

    private void stopEmulator() {
        l.lifecycle("Stopping emulator ${testConf.emulatorName} and logcat")
        emulatorProcess?.destroy()
        logcatProcess?.destroy()
        l.lifecycle("Stopped emulator ${testConf.emulatorName} and logcat")
    }
}
