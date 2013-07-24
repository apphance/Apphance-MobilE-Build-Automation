package com.apphance.flow.configuration.android

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.executor.AndroidExecutor
import com.apphance.flow.plugins.android.parsers.AndroidBuildXmlHelper
import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper
import org.gradle.api.GradleException
import org.gradle.api.Project

import javax.inject.Inject
import java.nio.file.Files

import static com.apphance.flow.util.file.FileManager.relativeTo
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

    def testDir = new FileProperty(
            name: 'android.dir.test',
            message: 'Directory where test sources are located. By convention this folder should have "robolectric" subfolder if project has ' +
                    'robolectric tests',
            defaultValue: { relativeTo(project.rootDir.absolutePath, project.file('test').absolutePath) },
            validator: {
                def file = new File(conf.rootDir, it as String)
                Files.isDirectory(file.toPath())
                Files.isDirectory(new File(file, 'robolectric').toPath())
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
        check testDir.validator(testDir.value), "Incorrect value '${testDir.value}' of property ${testDir.name}. Check that directory exists and contains " +
                "'robolectric' subdirectory"
    }
}
