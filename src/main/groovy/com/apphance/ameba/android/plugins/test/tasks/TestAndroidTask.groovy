package com.apphance.ameba.android.plugins.test.tasks

import com.apphance.ameba.android.AndroidManifestHelper
import com.apphance.ameba.android.AndroidProjectConfiguration
import com.apphance.ameba.android.plugins.test.AndroidTestConfiguration
import com.apphance.ameba.executor.AntExecutor
import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.util.file.FileManager
import org.gradle.api.GradleException
import org.gradle.api.Project

import static com.apphance.ameba.PropertyCategory.readProperty
import static com.apphance.ameba.android.AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration
import static com.apphance.ameba.android.plugins.test.AndroidTestConfigurationRetriever.getAndroidTestConfiguration
import static com.apphance.ameba.android.plugins.test.AndroidTestProperty.MOCK_LOCATION
import static com.apphance.ameba.executor.AntExecutor.CLEAN
import static com.apphance.ameba.executor.AntExecutor.INSTRUMENT
import static org.gradle.api.logging.Logging.getLogger

class TestAndroidTask {

    public static final String TEST_RUNNER = "pl.polidea.instrumentation.PolideaInstrumentationTestRunner"

    private l = getLogger(getClass())

    private Project project
    private AndroidProjectConfiguration androidConf
    private AndroidTestConfiguration androidTestConf
    private CommandExecutor executor
    private AndroidManifestHelper androidManifestHelper = new AndroidManifestHelper()
    Process emulatorProcess
    Process logcatProcess

    TestAndroidTask(Project project, CommandExecutor executor) {
        this.project = project
        this.androidConf = getAndroidProjectConfiguration(project)
        this.androidTestConf = getAndroidTestConfiguration(project)
        this.executor = executor
    }

    void testAndroid() {
        // TODO: what to do when no test directory exists ?? should I automatically make one ??
        if (!androidTestConf.androidTestDirectory.exists()) {
            println "Test directory not found. Please run gradle prepareRobotium in order to create simple Robotium project. Aborting"
            return
        }
        prepareTestBuilds()
        startEmulator(androidTestConf.emulatorNoWindow)
        try {
            installTestBuilds()
            runAndroidTests()
        } finally {
            stopEmulator()
        }
    }

    private prepareTestBuilds() {
        androidTestConf.coverageDir.delete()
        androidTestConf.coverageDir.mkdirs()
        androidTestConf.coverageDir.mkdir()
        // empty raw dir first
        if (androidTestConf.rawDir.exists()) {
            deleteNonEmptyDirectory(androidTestConf.rawDir)
        }
        androidTestConf.rawDir.mkdir()
        def commandAndroid = [
                'android',
                'update',
                'test-project',
                '-p',
                '.',
                '-m',
                '../../'
        ]
        executor.executeCommand(new Command(runDir: androidTestConf.androidTestDirectory, cmd: commandAndroid))
        boolean useMockLocation = readProperty(project, MOCK_LOCATION).toString().toBoolean()
        if (useMockLocation) {
            androidManifestHelper.addPermissions(project.rootDir, 'android.permission.ACCESS_MOCK_LOCATION')
        }
        try {
            def antExecutor = new AntExecutor(androidTestConf.androidTestDirectory)
            antExecutor.executeTarget(CLEAN, ['test.runner': TEST_RUNNER])
            antExecutor.executeTarget(INSTRUMENT, ['test.runner': TEST_RUNNER])
            File localEmFile = new File(androidTestConf.androidTestDirectory, 'coverage.em')
            if (localEmFile.exists()) {
                boolean res = localEmFile.renameTo(androidTestConf.coverageEmFile)
                l.lifecycle("Renamed ${localEmFile} to ${androidTestConf.coverageEmFile} with result: ${res}")
            } else {
                l.lifecycle("No ${localEmFile}. Not renaming to ${androidTestConf.coverageEmFile}")
            }
        } finally {
            if (useMockLocation) {
                androidManifestHelper.restoreOriginalManifest(project.rootDir)
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
        l.lifecycle("Starting emulator ${androidTestConf.emulatorName}")
        androidTestConf.emulatorPort = findFreeEmulatorPort()
        def emulatorCommand = [
                'emulator',
                '-avd',
                androidTestConf.emulatorName,
                '-port',
                androidTestConf.emulatorPort,
                '-no-boot-anim'
        ]
        if (noWindow) {
            emulatorCommand << '-no-window'
        }
        emulatorProcess = executor.startCommand(new Command(runDir: project.rootDir, cmd: emulatorCommand))
        Thread.sleep(4 * 1000) // sleep for some time.
        runLogCat(project)
        waitUntilEmulatorReady()
        l.lifecycle("Started emulator ${androidTestConf.emulatorName}")
    }

    private int findFreeEmulatorPort() {
        int startPort = 5554
        int endPort = 5584
        for (int port = startPort; port <= endPort; port += 2) {
            l.lifecycle("Android emulator probing. trying ports: ${port} ${port + 1}")
            try {
                ServerSocket ss1 = new ServerSocket(port, 0, Inet4Address.getByAddress([127, 0, 0, 1] as byte[]))
                try {
                    ss1.setReuseAddress(true)
                    ServerSocket ss2 = new ServerSocket(port + 1, 0, Inet4Address.getByAddress([127, 0, 0, 1] as byte[]))
                    try {
                        ss2.setReuseAddress(true)
                        l.lifecycle("Success! ${port} ${port + 1} are free")
                        return port
                    } finally {
                        ss2.close()
                    }
                } finally {
                    ss1.close()
                }
            } catch (IOException e) {
                l.lifecycle("Could not obtain ports ${port} ${port + 1}")
            }
        }
        throw new GradleException("Could not find free emulator port (tried all from ${startPort} to ${endPort}!... ")
    }

    private void runLogCat(Project project) {
        l.lifecycle("Starting logcat monitor on ${androidTestConf.emulatorName}")
        String[] commandRunLogcat = [
                androidTestConf.adbBinary,
                '-s',
                "emulator-${androidTestConf.emulatorPort}",
                'logcat',
                '-v',
                'time'
        ]
        logcatProcess = executor.startCommand(new Command(runDir: project.rootDir, cmd: commandRunLogcat))
    }

    private void waitUntilEmulatorReady() {
        l.lifecycle("Waiting until emulator is ready ${androidTestConf.emulatorName}")
        String[] commandRunShell = [
                androidTestConf.adbBinary,
                '-s',
                "emulator-${androidTestConf.emulatorPort}",
                'shell',
                'getprop',
                'dev.bootcomplete'
        ]
        def startTime = System.currentTimeMillis()
        while (true) {
            def res = executor.executeCommand(new Command(runDir: project.rootDir, cmd: commandRunShell, failOnError: false))
            if (res != null && res[0] == "1") {
                l.lifecycle("Emulator is ready ${androidTestConf.emulatorName}!")
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
        executor.executeCommand(new Command(runDir: androidTestConf.androidTestDirectory, cmd: [
                androidTestConf.adbBinary,
                '-s',
                "emulator-${androidTestConf.emulatorPort}",
                'uninstall',
                androidConf.mainProjectPackage
        ], failOnError: false))
        executor.executeCommand(new Command(runDir: androidTestConf.androidTestDirectory, cmd: [
                androidTestConf.adbBinary,
                '-s',
                "emulator-${androidTestConf.emulatorPort}",
                'uninstall',
                androidTestConf.testProjectPackage
        ], failOnError: false))
        executor.executeCommand(new Command(runDir: project.rootDir, cmd: [
                androidTestConf.adbBinary,
                '-s',
                "emulator-${androidTestConf.emulatorPort}",
                'install',
                "bin/${androidConf.mainProjectName}-debug.apk"
        ]))
        executor.executeCommand(new Command(runDir: androidTestConf.androidTestDirectory, cmd: [
                androidTestConf.adbBinary,
                '-s',
                "emulator-${androidTestConf.emulatorPort}",
                'install',
                "bin/${androidTestConf.testProjectName}-instrumented.apk"
        ]))
    }

    private void runAndroidTests() {
        l.lifecycle("Running tests on ${androidTestConf.emulatorName}")
        if (androidTestConf.testPerPackage) {
            def allPackages = []
            FileManager.findAllPackages("", new File(androidTestConf.androidTestDirectory, "src"), allPackages)
            l.lifecycle("Running tests on packages ${allPackages}")
            allPackages.each {
                l.lifecycle("Running tests for package ${it}")
                def commandRunTests = prepareTestCommandLine(it)
                l.lifecycle("Running  ${commandRunTests}")
                executor.executeCommand(new Command(runDir: androidTestConf.androidTestDirectory, cmd: commandRunTests))
            }
        } else {
            def commandRunTests = prepareTestCommandLine(null)
            executor.executeCommand(new Command(runDir: androidTestConf.androidTestDirectory, cmd: commandRunTests))
        }
        if (androidTestConf.useEmma) {
            extractEmmaCoverage()
            prepareCoverageReport()
        }
        extractXMLJUnitFiles()
    }

    private String[] prepareTestCommandLine(String packageName) {
        def commandLine = [
                androidTestConf.adbBinary,
                '-s',
                "emulator-${androidTestConf.emulatorPort}",
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
        if (androidTestConf.useEmma) {
            commandLine += [
                    '-e',
                    'coverage',
                    'true',
                    '-e',
                    'coverageFile',
                    androidTestConf.emmaDumpFile,
            ]
        }
        commandLine << "${androidTestConf.testProjectPackage}/${TEST_RUNNER}"
        return (String[]) commandLine
    }

    private void extractEmmaCoverage() {
        l.lifecycle("Extracting coverage report from ${androidTestConf.emulatorName}")
        String[] commandDownloadCoverageFile = [
                androidTestConf.adbBinary,
                '-s',
                "emulator-${androidTestConf.emulatorPort}",
                'pull',
                androidTestConf.emmaDumpFile,
                androidTestConf.coverageEcFile
        ]
        executor.executeCommand(new Command(runDir: androidTestConf.androidTestDirectory, cmd: commandDownloadCoverageFile))
        l.lifecycle("Pulled coverage report from ${androidTestConf.emulatorName} to ${androidTestConf.coverageDir}...")
    }

    private void prepareCoverageReport() {
        project.ant.taskdef(resource: "emma_ant.properties",
                classpath: project.configurations.emma.asPath)
        project.ant.emma {
            report(sourcepath: "${project.rootDir}/src") {
                fileset(dir: androidTestConf.coverageDir) {
                    include(name: 'coverage.ec*')
                    include(name: 'coverage.em')
                }
                xml(outfile: "${androidTestConf.coverageDir}/android_coverage.xml")
                txt(outfile: "${androidTestConf.coverageDir}/android_coverage.txt")
                html(outfile: "${androidTestConf.coverageDir}/android_coverage.html")
            }
        }
        l.lifecycle("Prepared coverage report from ${androidTestConf.emulatorName} to ${androidTestConf.coverageDir}...")
    }

    private void extractXMLJUnitFiles() {
        l.lifecycle("Extracting coverage report from ${androidTestConf.emulatorName}")
        String[] commandDownloadXmlFile = [
                androidTestConf.adbBinary,
                '-s',
                "emulator-${androidTestConf.emulatorPort}",
                'pull',
                androidTestConf.xmlJUnitDir
        ]
        executor.executeCommand(new Command(rawDir: androidTestConf.coverageDir, cmd: commandDownloadXmlFile))
    }

    private void stopEmulator() {
        l.lifecycle("Stopping emulator ${androidTestConf.emulatorName} and logcat")
        emulatorProcess?.destroy()
        logcatProcess?.destroy()
        l.lifecycle("Stopped emulator ${androidTestConf.emulatorName} and logcat")
    }
}
