package com.apphance.flow.plugins.android.test.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidTestConfiguration
import com.apphance.flow.executor.AntExecutor
import com.apphance.flow.executor.ExecutableCommand
import com.apphance.flow.executor.command.Command
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper
import com.apphance.flow.util.file.FileManager
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject
import javax.inject.Named

import static android.Manifest.permission.ACCESS_MOCK_LOCATION
import static com.apphance.flow.executor.AntExecutor.CLEAN
import static com.apphance.flow.executor.AntExecutor.INSTRUMENT
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_TEST

class TestAndroidTask extends DefaultTask {

    public static final String TEST_RUNNER = "pl.polidea.instrumentation.PolideaInstrumentationTestRunner"

    static String NAME = 'testAndroid'
    String group = FLOW_TEST
    String description = 'Runs android tests on the project'

    @Inject AndroidConfiguration conf
    @Inject AndroidTestConfiguration testConf
    @Inject CommandExecutor executor
    @Inject AntExecutor antExecutor
    @Inject AndroidManifestHelper manifestHelper
    @Inject
    @Named('executable.android') ExecutableCommand executableAndroid
    @Inject
    @Named('executable.emulator') ExecutableCommand executableEmulator
    @Inject
    @Named('executable.emulator') ExecutableCommand executableAdb

    private Process emulatorProcess
    private Process logcatProcess

    @TaskAction
    void testAndroid() {
        if (!testConf.testDir.value?.exists()) {
            logger.lifecycle("Test directory not found. Please run gradle prepareRobotium in order to create simple Robotium project. Aborting")
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
        executor.executeCommand(new Command(runDir: testConf.testDir.value, cmd: executableAndroid.cmd + [
                'update',
                'test-project',
                '-p',
                '.',
                '-m',
                '../../'
        ]))
        boolean useMockLocation = testConf.mockLocation.value
        if (useMockLocation) {
            manifestHelper.addPermissions(conf.rootDir, ACCESS_MOCK_LOCATION)
        }
        try {
            antExecutor.executeTarget testConf.testDir.value, CLEAN, ['test.runner': TEST_RUNNER]
            antExecutor.executeTarget testConf.testDir.value, INSTRUMENT, ['test.runner': TEST_RUNNER]
            File localEmFile = new File(testConf.testDir.value, 'coverage.em')
            if (localEmFile.exists()) {
                boolean res = localEmFile.renameTo(testConf.coverageEMFile)
                logger.lifecycle("Renamed ${localEmFile} to ${testConf.coverageEMFile} with result: ${res}")
            } else {
                logger.lifecycle("No ${localEmFile}. Not renaming to ${testConf.coverageEMFile}")
            }
        } finally {
            if (useMockLocation) {
                manifestHelper.restoreOriginalManifest(conf.rootDir)
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
        logger.lifecycle("Starting emulator ${testConf.emulatorName}")
        def emulatorCommand = executableEmulator.cmd + [
                '-avd',
                testConf.emulatorName,
                '-port',
                testConf.emulatorPort,
                '-no-boot-anim'
        ]
        if (noWindow) {
            emulatorCommand << '-no-window'
        }
        emulatorProcess = executor.startCommand(new Command(runDir: conf.rootDir, cmd: emulatorCommand))
        Thread.sleep(4 * 1000) // sleep for some time.
        runLogCat()
        waitUntilEmulatorReady()
        logger.lifecycle("Started emulator ${testConf.emulatorName}")
    }

    private void runLogCat() {
        logger.lifecycle("Starting logcat monitor on ${testConf.emulatorName}")
        logcatProcess = executor.startCommand(new Command(runDir: conf.rootDir, cmd: executableAdb.cmd + [
                '-s',
                "emulator-${testConf.emulatorPort}",
                'logcat',
                '-v',
                'time'
        ]))
    }

    private void waitUntilEmulatorReady() {
        logger.lifecycle("Waiting until emulator is ready ${testConf.emulatorName}")
        String[] commandRunShell = executableAdb.cmd + [
                '-s',
                "emulator-${testConf.emulatorPort}",
                'shell',
                'getprop',
                'dev.bootcomplete'
        ]
        def startTime = System.currentTimeMillis()
        while (true) {
            def res = executor.executeCommand(new Command(runDir: conf.rootDir, cmd: commandRunShell, failOnError: false))
            if (res != null && res[0] == "1") {
                logger.lifecycle("Emulator is ready ${testConf.emulatorName}!")
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
        executor.executeCommand(new Command(runDir: testConf.testDir.value, cmd: executableAdb.cmd + [
                '-s',
                "emulator-${testConf.emulatorPort}",
                'uninstall',
                conf.mainPackage
        ], failOnError: false))
        executor.executeCommand(new Command(runDir: testConf.testDir.value, cmd: executableAdb.cmd + [
                '-s',
                "emulator-${testConf.emulatorPort}",
                'uninstall',
                testConf.testProjectPackage
        ], failOnError: false))
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: executableAdb.cmd + [
                '-s',
                "emulator-${testConf.emulatorPort}",
                'install',
                "bin/${conf.projectName.value}-debug.apk"
        ]))
        executor.executeCommand(new Command(runDir: testConf.testDir.value, cmd: executableAdb.cmd + [
                '-s',
                "emulator-${testConf.emulatorPort}",
                'install',
                "bin/${testConf.testProjectName}-instrumented.apk"
        ]))
    }

    private void runAndroidTests() {
        logger.lifecycle("Running tests on ${testConf.emulatorName}")
        if (testConf.testPerPackage) {
            def allPackages = []
            FileManager.findAllPackages("", new File(testConf.getTestDir().value, "src"), allPackages)
            logger.lifecycle("Running tests on packages ${allPackages}")
            allPackages.each {
                logger.lifecycle("Running tests for package ${it}")
                def commandRunTests = prepareTestCommandLine(it)
                logger.lifecycle("Running  ${commandRunTests}")
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
        def commandLine = executableAdb.cmd + [
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
        logger.lifecycle("Extracting coverage report from ${testConf.emulatorName}")
        executor.executeCommand(new Command(runDir: testConf.testDir.value, cmd: executableAdb.cmd + [
                '-s',
                "emulator-${testConf.emulatorPort}",
                'pull',
                testConf.emmaDumpFilePath,
                testConf.coverageECFile
        ]))
        logger.lifecycle("Pulled coverage report from ${testConf.emulatorName} to ${testConf.coverageDir}...")
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
        logger.lifecycle("Prepared coverage report from ${testConf.emulatorName} to ${testConf.coverageDir}...")
    }

    private void extractXMLJUnitFiles() {
        logger.lifecycle("Extracting coverage report from ${testConf.emulatorName}")
        executor.executeCommand(new Command(runDir: testConf.coverageDir, cmd: executableAdb.cmd + [
                '-s',
                "emulator-${testConf.emulatorPort}",
                'pull',
                testConf.XMLJUnitDirPath
        ]))
    }

    private void stopEmulator() {
        logger.lifecycle("Stopping emulator ${testConf.emulatorName} and logcat")
        emulatorProcess?.destroy()
        logcatProcess?.destroy()
        logger.lifecycle("Stopped emulator ${testConf.emulatorName} and logcat")
    }
}
