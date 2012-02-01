package com.apphance.ameba.android;

import org.gradle.api.Project
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import com.sun.org.apache.xpath.internal.XPathAPI


public class AndroidProjectConfigurationRetriever {
    static Logger logger = Logging.getLogger(AndroidProjectConfigurationRetriever.class)
    AndroidManifestHelper androidManifestHelper = new AndroidManifestHelper()
    AndroidBuildXmlHelper buildXmlHelper = new AndroidBuildXmlHelper()
    AndroidProjectConfiguration getAndroidProjectConfiguration(final Project project){
        if (!project.hasProperty('android.project.configuration')) {
            project['android.project.configuration'] = new AndroidProjectConfiguration()
        }
        return project['android.project.configuration']
    }

    void readAndroidProjectConfiguration(Project project) {
        AndroidProjectConfiguration androidConf = getAndroidProjectConfiguration(project)
        androidConf.mainVariant = project.hasProperty('android.mainVariant') && !project['android.mainVariant'].empty ?
                project['android.mainVariant'] : androidConf.variants[0]
        androidConf.emulatorSkin = project.hasProperty('android.test.emulator.skin') ?
                project['android.test.emulator.skin'] : 'WVGA800'
        androidConf.emulatorCardSize = project.hasProperty('android.test.emulator.cardSize') ?
                project['android.test.emulator.cardSize'] : '200M'
        androidConf.emulatorSnapshotsEnabled = project.hasProperty('android.test.emulator.snapshotEnabled') ?
                Boolean.parseBoolean(project['android.test.emulator.snapshotEnabled']) : true
        androidConf.emulatorNoWindow = project.hasProperty('android.test.emulator.noWindow') ?
                Boolean.parseBoolean(project['android.test.emulator.noWindow']) : true
        androidConf.emulatorUseVNC = project.hasProperty('android.test.emulator.useVNC') ?
                Boolean.parseBoolean(project['android.test.emulator.withVNC']) : true
        androidConf.emulatorName= new File('.').getAbsolutePath().replaceAll('[\\\\ /]','_')
        androidConf.testPerPackage = project.hasProperty('android.test.perPackage') ?
                Boolean.parseBoolean(project['android.test.perPackage']) : false
        androidConf.useEmma = project.hasProperty('android.useEmma') ?
                Boolean.parseBoolean(project['android.useEmma']) : true
        def mainProjectManifest = androidManifestHelper.getParsedManifest(project.rootDir)
        androidConf.mainProjectPackage = XPathAPI.selectSingleNode(mainProjectManifest, "/manifest/@package").nodeValue
        androidConf.mainProjectName = buildXmlHelper.readProjectName(project.rootDir)
    }
}
