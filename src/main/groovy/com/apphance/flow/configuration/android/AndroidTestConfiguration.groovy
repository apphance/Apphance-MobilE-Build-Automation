package com.apphance.flow.configuration.android

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.properties.BooleanProperty
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.AndroidExecutor
import com.apphance.flow.plugins.android.parsers.AndroidBuildXmlHelper
import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper
import org.gradle.api.GradleException
import org.gradle.api.Project

import javax.inject.Inject

import static java.io.File.separator
import static java.net.InetAddress.getByName

@com.google.inject.Singleton
class AndroidTestConfiguration extends AbstractConfiguration {

    String configurationName = 'Android Test Configuration'

    private final List<String> BOOLEAN_VALUES = ['true', 'false']
    private final Closure<Boolean> BOOLEAN_VALIDATOR = { it in BOOLEAN_VALUES }

    private boolean enabledInternal = false

    @Inject Project project
    @Inject AndroidConfiguration conf
    @Inject AndroidManifestHelper manifestHelper
    @Inject AndroidBuildXmlHelper buildXmlHelper
    @Inject AndroidExecutor androidExecutor

    @Override
    boolean isEnabled() {
        enabledInternal && conf.isEnabled()
    }

    @Override
    void setEnabled(boolean enabled) {
        enabledInternal = enabled
    }

    def emulatorTarget = new StringProperty(
            name: 'android.test.emulator.target',
            message: 'Target of the emulator',
            defaultValue: { conf.target.value },
            possibleValues: { possibleTargets() },
            validator: { it in possibleTargets() }
    )

    def emulatorSkin = new StringProperty(
            name: 'android.test.emulator.skin',
            message: 'Android emulator skin',
            defaultValue: { androidExecutor.defaultSkinForTarget(emulatorTarget.value) },
            possibleValues: { possibleSkins() },
            validator: { it in possibleSkins() }
    )

    private List<String> possibleTargets() {
        androidExecutor.targets
    }

    private List<String> possibleSkins() {
        androidExecutor.skinsForTarget(emulatorTarget.value).findAll { !it?.trim()?.empty }
    }

    def emulatorCardSize = new StringProperty(
            name: 'android.test.emulator.card.size',
            message: 'Size of the SD card attached to emulator',
            defaultValue: { '200M' },
            validator: { it?.matches('[0-9]+[KM]') }
    )

    def emulatorSnapshotEnabled = new BooleanProperty(
            name: 'android.test.emulator.snapshot.enabled',
            message: 'Flag specifying if emulator uses snapshots (much faster)',
            defaultValue: { true },
            validator: BOOLEAN_VALIDATOR,
            possibleValues: { BOOLEAN_VALUES }
    )

    def emulatorNoWindow = new BooleanProperty(
            name: 'android.test.emulator.no.window',
            message: 'Flag specifying if no-window option should be used with emulator',
            defaultValue: { true },
            validator: BOOLEAN_VALIDATOR,
            possibleValues: { BOOLEAN_VALUES }
    )

    def testDir = new FileProperty(
            name: 'android.dir.test',
            message: 'Directory where Robotium test project is located',
            defaultValue: { project.file("test${separator}android".toString()) },
            validator: {
                def file = new File(conf.rootDir, it as String)
                file?.absolutePath?.trim() ? (file.exists() && file.isDirectory() && file.canWrite()) : false
            }
    )

    String getTestProjectPackage() {
        if (testDir.value?.exists()) {
            return manifestHelper.androidPackage(testDir.value)
        }
        null
    }

    String getTestProjectName() {
        if (testDir.value?.exists()) {
            return buildXmlHelper.projectName(testDir.value)
        }
        null
    }

    String getEmulatorName() {
        conf.rootDir.getAbsolutePath().replaceAll('[\\\\ /]', '_')
    }

    def testPerPackage = new BooleanProperty(
            name: 'android.test.per.package',
            message: 'Flag specifying if tests should be run per package. If false, then all are run at once',
            defaultValue: { false },
            validator: BOOLEAN_VALIDATOR,
            possibleValues: { BOOLEAN_VALUES }
    )

    def mockLocation = new BooleanProperty(
            name: 'android.test.mock.location',
            message: 'Whether the test application should be build with location mocking enabled (for testing location-based apps)',
            defaultValue: { false },
            validator: BOOLEAN_VALIDATOR,
            possibleValues: { BOOLEAN_VALUES }
    )

    File getRawDir() {
        project.file("res${separator}raw".toString())
    }

    File getAVDDir() {
        project.file('avds')
    }

    @Lazy Integer emulatorPort = {
        findFreeEmulatorPort()
    }()

    private int findFreeEmulatorPort() {
        int startPort = 5554
        int endPort = 5584
        InetAddress localhost = getByName('localhost')
        for (int port = startPort; port <= endPort; port += 2) {
            def ss1 = null, ss2 = null
            try {
                ss1 = new ServerSocket(port, 0, localhost)
                ss1.reuseAddress = true
                ss2 = new ServerSocket(port + 1, 0, localhost)
                ss1.reuseAddress = true
                return port
            } catch (e) {
            } finally {
                [ss1, ss2].collect { it?.close() }
            }
        }
        throw new GradleException("Could not find free emulator port (tried all from ${startPort} to ${endPort}!")
    }

    def emmaEnabled = new BooleanProperty(
            name: 'android.test.emma.enabled',
            message: 'Whether emma test coverage should be run',
            defaultValue: { true },
            validator: BOOLEAN_VALIDATOR,
            possibleValues: { BOOLEAN_VALUES }
    )

    String getEmmaDumpFilePath() {
        "/data/data/${conf.mainPackage}/coverage.ec"
    }

    File getCoverageDir() {
        project.file("tmp${separator}coverage")
    }

    File getCoverageECFile() {
        new File(coverageDir, 'coverage.ec')
    }

    File getCoverageEMFile() {
        new File(coverageDir, 'coverage.em')
    }

    String getXMLJUnitDirPath() {
        "/data/data/${conf.mainPackage}/files/"
    }

    @Override
    void checkProperties() {
        check emulatorTarget.validator(emulatorTarget.value), "Property '${emulatorTarget.name}' is not valid! Should be valid android target!"
        check emulatorSkin.validator(emulatorSkin.value), "Property '${emulatorSkin.name}' is not valid! Should be valid android skin!"
        check emulatorCardSize.validator(emulatorCardSize.value), "Property '${emulatorCardSize.name}' is not valid! Should match <NUMBER>[K|M]"
        check !(emulatorSnapshotEnabled.validator(emulatorSnapshotEnabled.value)), "Property '${emulatorSnapshotEnabled.name}' is not valid! Should match one of ${BOOLEAN_VALUES}"
        check !(emulatorNoWindow.validator(emulatorNoWindow.value)), "Property '${emulatorNoWindow.name}' is not valid! Should match one of ${BOOLEAN_VALUES}"
        check !(testPerPackage.validator(testPerPackage.value)), "Property '${testPerPackage.name}' is not valid! Should match one of ${BOOLEAN_VALUES}"
        check !(mockLocation.validator(mockLocation.value)), "Property '${mockLocation.name}' is not valid! Should match one of ${BOOLEAN_VALUES}"
        check !(emmaEnabled.validator(emmaEnabled.value)), "Property '${emmaEnabled.name}' is not valid! Should match one of ${BOOLEAN_VALUES}"
//        check testDir.validator(testDir.value), "Property '${testDir.name}' is not valid! Should be valid directory name!"//TODO not all projects run tests
    }
}
