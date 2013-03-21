package com.apphance.ameba.plugins.android.test.tasks

import com.apphance.ameba.plugins.android.AndroidBuildXmlHelper
import com.apphance.ameba.plugins.android.AndroidManifestHelper
import com.apphance.ameba.plugins.android.AndroidProjectConfiguration
import com.apphance.ameba.plugins.android.test.AndroidTestConfiguration
import org.gradle.api.Project

import static com.apphance.ameba.PropertyCategory.readProperty
import static com.apphance.ameba.plugins.android.AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration
import static com.apphance.ameba.plugins.android.test.AndroidTestConfiguration.AVD_PATH
import static com.apphance.ameba.plugins.android.test.AndroidTestConfigurationRetriever.getAndroidTestConfiguration
import static com.apphance.ameba.plugins.android.test.AndroidTestProperty.*

class ReadAndroidTestConfigurationTask {

    private Project project
    private AndroidProjectConfiguration androidConf
    private AndroidTestConfiguration androidTestConf
    private AndroidManifestHelper androidManifestHelper = new AndroidManifestHelper()
    private AndroidBuildXmlHelper androidBuildXmlHelper = new AndroidBuildXmlHelper()

    ReadAndroidTestConfigurationTask(Project project) {
        this.project = project
        this.androidConf = getAndroidProjectConfiguration(project)
        this.androidTestConf = getAndroidTestConfiguration(project)
    }

    void readAndroidConfiguration() {
        androidTestConf.rawDir = project.file('res/raw')
        androidTestConf.emulatorName = project.rootDir.getAbsolutePath().replaceAll('[\\\\ /]', '_')
        androidTestConf.emulatorTargetName = readProperty(project, EMULATOR_TARGET)
        if (androidTestConf.emulatorTargetName == null || androidTestConf.emulatorTargetName.empty) {
            androidTestConf.emulatorTargetName = androidConf.targetName
        }
        androidTestConf.androidTestDirectory = project.file(readProperty(project, TEST_DIRECTORY))
        if (androidTestConf.androidTestDirectory.exists()) {
            androidTestConf.testProjectPackage = androidManifestHelper.androidPackage(androidTestConf.androidTestDirectory)
            androidTestConf.testProjectName = androidBuildXmlHelper.projectName(androidTestConf.androidTestDirectory)
        }
        androidTestConf.emulatorSkin = readProperty(project, EMULATOR_SKIN)
        androidTestConf.emulatorCardSize = readProperty(project, EMULATOR_CARDSIZE)
        androidTestConf.emulatorSnapshotsEnabled = readProperty(project, EMULATOR_SNAPSHOT_ENABLED).toBoolean()
        androidTestConf.useEmma = readProperty(project, USE_EMMA).toBoolean()
        androidTestConf.testPerPackage = readProperty(project, TEST_PER_PACKAGE).toBoolean()
        androidTestConf.emulatorNoWindow = readProperty(project, EMULATOR_NO_WINDOW).toBoolean()

        project.configurations.add('emma')
        project.dependencies.add('emma', project.files([
                new File(androidConf.sdkDirectory, 'tools/lib/emma.jar')
        ]))
        project.dependencies.add('emma', project.files([
                new File(androidConf.sdkDirectory, 'tools/lib/emma_ant.jar')
        ]))
        androidTestConf.emmaDumpFile = "/data/data/${androidConf.mainProjectPackage}/coverage.ec"
        androidTestConf.xmlJUnitDir = "/data/data/${androidConf.mainProjectPackage}/files/"
        androidTestConf.coverageDir = project.file('tmp/coverage')
        androidTestConf.coverageEmFile = new File(androidTestConf.coverageDir, 'coverage.em')
        androidTestConf.coverageEcFile = new File(androidTestConf.coverageDir, 'coverage.ec')
        androidTestConf.adbBinary = new File(androidConf.sdkDirectory, 'platform-tools/adb')
        androidTestConf.androidBinary = new File(androidConf.sdkDirectory, 'tools/android')
        androidTestConf.avdDir = project.file(AVD_PATH)
    }
}
