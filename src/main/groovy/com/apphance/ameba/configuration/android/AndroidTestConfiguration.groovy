package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.properties.BooleanProperty
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.plugins.android.AndroidBuildXmlHelper
import com.apphance.ameba.plugins.android.AndroidManifestHelper
import org.gradle.api.GradleException
import org.gradle.api.Project

import javax.inject.Inject

import static java.io.File.separator
import static java.net.InetAddress.getByAddress

@com.google.inject.Singleton
class AndroidTestConfiguration extends AbstractConfiguration {

    String configurationName = 'Android Test Configuration'

    private boolean enabledInternal = false
    private Integer emulatorPort

    private Project project
    private AndroidConfiguration androidConf
    private AndroidManifestHelper manifestHelper
    private AndroidBuildXmlHelper buildXmlHelper

    @Inject
    AndroidTestConfiguration(
            Project project,
            AndroidConfiguration androidConf,
            AndroidManifestHelper manifestHelper,
            AndroidBuildXmlHelper buildXmlHelper) {
        this.project = project
        this.androidConf = androidConf
        this.manifestHelper = manifestHelper
        this.buildXmlHelper = buildXmlHelper
    }

    @Override
    boolean isEnabled() {
        enabledInternal && androidConf.isEnabled()
    }

    @Override
    void setEnabled(boolean enabled) {
        enabledInternal = enabled
    }

    def emulatorSkin = new StringProperty(
            name: 'android.test.emulator.skin',
            message: 'Android emulator skin',
            defaultValue: { 'WVGA800' }
    )

    def emulatorTarget = new StringProperty(
            name: 'android.test.emulator.target',
            message: 'Target of the emulator',
            defaultValue: { androidConf.target.value }
    )

    def emulatorCardSize = new StringProperty(
            name: 'android.test.emulator.card.size',
            message: 'Size of the SD card attached to emulator',
            defaultValue: { '200M' }
    )

    def emulatorSnapshotEnabled = new BooleanProperty(
            name: 'android.test.emulator.snapshot.enabled',
            message: 'Flag specifying if emulator uses snapshots (much faster)',
            defaultValue: { true }
    )

    def emulatorNoWindow = new BooleanProperty(
            name: 'android.test.emulator.no.window',
            message: 'Flag specifying if no-window option should be used with emulator',
            defaultValue: { true }
    )

    def testDir = new FileProperty(
            name: 'android.dir.test',
            message: 'Directory where Robotium test project is located',
            defaultValue: { project.file("android${separator}test".toString()) }
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
        project.rootDir.getAbsolutePath().replaceAll('[\\\\ /]', '_')
    }

    def testPerPackage = new BooleanProperty(
            name: 'android.test.per.package',
            message: 'Flag specifying if tests should be run per package. If false, then all are run at once',
            defaultValue: { false }
    )

    def mockLocation = new BooleanProperty(
            name: 'android.test.mock.location',
            message: 'Whether the test application should be build with location mocking enabled (for testing location-based apps)',
            defaultValue: { false }
    )

    File getRawDir() {
        project.file("res${separator}raw".toString())
    }

    File getAVDDir() {
        project.file('avds')
    }

    Integer getEmulatorPort() {
        if (!emulatorPort) {
            emulatorPort = findFreeEmulatorPort()
        }
        emulatorPort
    }

    private int findFreeEmulatorPort() {
        int startPort = 5554
        int endPort = 5584
        for (int port = startPort; port <= endPort; port += 2) {
//            l.lifecycle("Android emulator probing. trying ports: ${port} ${port + 1}")
            try {
                ServerSocket ss1 = new ServerSocket(port, 0, getByAddress([127, 0, 0, 1] as byte[]))
                try {
                    ss1.setReuseAddress(true)
                    ServerSocket ss2 = new ServerSocket(port + 1, 0, getByAddress([127, 0, 0, 1] as byte[]))
                    try {
                        ss2.setReuseAddress(true)
//                        l.lifecycle("Success! ${port} ${port + 1} are free")
                        return port
                    } finally {
                        ss2.close()
                    }
                } finally {
                    ss1.close()
                }
            } catch (IOException e) {
//                l.lifecycle("Could not obtain ports ${port} ${port + 1}")
            }
        }
        throw new GradleException("Could not find free emulator port (tried all from ${startPort} to ${endPort}!... ")
    }

    def emmaEnabled = new BooleanProperty(
            name: 'android.test.emma.enabled',
            message: 'Whether emma test coverage should be run',
            defaultValue: { true }
    )

    File getADBBinary() {
        new File(androidConf.sdkDir.value, "platform${separator}tools")
    }

    File getAndroidBinary() {
        new File(androidConf.sdkDir.value, "tools${separator}android")
    }

    String getEmmaDumpFilePath() {
        "/data/data/${androidConf.mainPackage}/coverage.ec"
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
        "/data/data/${androidConf.mainPackage}/files/"
    }

}
