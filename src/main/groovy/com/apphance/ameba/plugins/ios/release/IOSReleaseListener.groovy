package com.apphance.ameba.plugins.ios.release

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.ios.IOSReleaseConfiguration
import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant
import com.apphance.ameba.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.ios.builder.IOSArtifactProvider
import com.apphance.ameba.plugins.ios.builder.IOSBuilderInfo
import com.apphance.ameba.plugins.ios.buildplugin.IOSBuildListener
import com.apphance.ameba.plugins.ios.parsers.MobileProvisionParser
import com.apphance.ameba.plugins.ios.parsers.PlistParser
import com.apphance.ameba.plugins.release.AmebaArtifact
import groovy.text.SimpleTemplateEngine
import org.gradle.api.AntBuilder

import javax.inject.Inject

import static com.apphance.ameba.configuration.ios.IOSConfiguration.FAMILIES
import static org.gradle.api.logging.Logging.getLogger

/**
 * Build listener for releases.
 *
 */
class IOSReleaseListener implements IOSBuildListener {

    def l = getLogger(getClass())

    @Inject
    CommandExecutor executor
    @Inject
    IOSConfiguration conf
    @Inject
    IOSReleaseConfiguration releaseConf
    @Inject
    AntBuilder ant
    @Inject
    IOSArtifactProvider artifactProvider
    @Inject
    IOSVariantsConfiguration variantsConf
    @Inject
    PlistParser plistParser
    @Inject
    MobileProvisionParser mpParser

    @Override
    void buildDone(IOSBuilderInfo bi) {
        if (conf.versionString != null) {
            if (bi.configuration != 'Debug') {
                prepareDistributionZipFile(bi)
                prepareDSYMZipFile(bi)
//                prepareAhSYMFiles(bi) //TODO turn on after DI is implemented
                prepareIpaFile(bi)
                prepareManifestFile(bi)
                prepareMobileProvisionFile(bi)
            } else {
                FAMILIES.each { family ->
                    prepareSimulatorBundleFile(bi, family)
                }
            }
        } else {
            l.lifecycle('Skipping building artifacts -> the build is not versioned')
        }
    }

    private void prepareDistributionZipFile(IOSBuilderInfo bi) {
        AmebaArtifact aa = prepareDistributionZipArtifact(bi)
        aa.location.parentFile.mkdirs()
        aa.location.delete()
        //TODO how to zip the file with new configuration?
//        ant.zip(destfile: distributionZipArtifact.location) {
//            zipfileset(dir: conf.distributionDir,
//                    includes: parser.findMobileProvisionFile(project, bi.target, bi.configuration).name)
//            zipfileset(dir: bi.buildDir, includes: "${bi.target}.app/**")
//        }
        l.lifecycle("Distribution zip file created: $aa")
    }

    private AmebaArtifact prepareDistributionZipArtifact(IOSBuilderInfo bi, boolean checkIfExists = false) {
        AmebaArtifact aa = artifactProvider.zipDistribution(bi)
        if (!checkIfExists || aa.location.exists()) {
            releaseConf.distributionZipFiles.put(bi.id, aa)
        } else {
            l.lifecycle("Skipping preparing distribution zip for ${bi} -> missing")
        }
        aa
    }

    private void prepareDSYMZipFile(IOSBuilderInfo bi) {
        AmebaArtifact aa = prepareDSYMZipArtifact(bi)
        aa.location.parentFile.mkdirs()
        aa.location.delete()
        ant.zip(destfile: aa.location) {
            zipfileset(dir: bi.buildDir, includes: "${bi.target}.app.dSYM/**")
        }
        l.lifecycle("dSYM zip file created: ${aa}")
    }

    private AmebaArtifact prepareDSYMZipArtifact(IOSBuilderInfo bi, boolean checkIfExists = false) {
        AmebaArtifact aa = artifactProvider.dSYMZip(bi)
        if (!checkIfExists || aa.location.exists()) {
            releaseConf.dSYMZipFiles.put(bi.id, aa)
        } else {
            l.lifecycle("Skipping preparing dSYM artifact for ${bi.id} : ${aa.location} -> missing")
        }
        aa
    }

    private void prepareAhSYMFiles(IOSBuilderInfo bi, boolean checkIfExists = false) {
//        AmebaArtifact ahSYM = prepareAhSymArtifacts(bi, checkIfExists)
//        ahSYM.location.delete()
//        ahSYM.location.mkdirs()
//        def je = new JythonExecutor()
//        def args = ['-p', conf.plistFile.canonicalPath, '-d', new File(bi.buildDir, "${bi.target}.app.dSYM").canonicalPath, '-o', new File(ahSYM.location.canonicalPath, bi.target).canonicalPath]
//        je.executeScript('jython/dump_reduce3_ameba.py', args)
//        l.lifecycle("ahSYM files created: ${ahSYM.location.list()}")
    }

    private AmebaArtifact prepareAhSymArtifacts(IOSBuilderInfo bi, boolean checkIfExists = false) {
        AmebaArtifact aa = artifactProvider.ahSYM(bi)
        if (!checkIfExists || aa.location.exists()) {
            releaseConf.ahSYMDirs.put(bi.id, aa)
        } else {
            l.lifecycle("Skipping preparing dSYM artifact for ${bi.id} : ${aa.location} -> missing")
        }
        aa
    }

    private void prepareIpaFile(IOSBuilderInfo bi) {
        AmebaArtifact aa = prepareIpaArtifact(bi)
        aa.location.parentFile.mkdirs()
        aa.location.delete()
        def appList = bi.buildDir.list([accept: { d, f -> f ==~ /.*\.app/ }] as FilenameFilter)
        def cmd = [
                '/usr/bin/xcrun',
                '-sdk',
                conf.sdk.value,
                'PackageApplication',
                '-v',
                new File(bi.buildDir, appList[0]).canonicalPath,
                '-o',
                aa.location.canonicalPath,
                '--embed',
                bi.mobileprovision.canonicalPath
        ]
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: cmd))
        l.lifecycle("ipa file created: $aa")
    }

    private AmebaArtifact prepareIpaArtifact(IOSBuilderInfo bi, boolean checkIfExists = false) {
        AmebaArtifact aa = artifactProvider.ipa(bi)
        if (!checkIfExists || aa.location.exists()) {
            releaseConf.ipaFiles.put(bi.id, aa)
        } else {
            l.lifecycle("Skipping preparing ipa artifact for ${bi.id} : ${aa.location} -> missing")
        }
        aa
    }

    private void prepareManifestFile(IOSBuilderInfo bi) {
        AmebaArtifact aa = prepareManifestArtifact(bi)
        aa.location.parentFile.mkdirs()
        aa.location.delete()

        URL manifestTemplate = this.class.getResource("manifest.plist")
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        def bundleId = plistParser.bundleId(bi.plist)
        def binding = [
                ipaUrl: releaseConf.ipaFiles.get(bi.id).url,
                title: bi.target,
                bundleId: bundleId
        ]
        l.lifecycle("Building manifest from ${bi.plist}, bundleId: ${bundleId}")
        def result = engine.createTemplate(manifestTemplate).make(binding)
        aa.location << (result.toString())
        l.lifecycle("Manifest file created: ${aa}")
    }

    private AmebaArtifact prepareManifestArtifact(IOSBuilderInfo bi, boolean checkIfExists = false) {
        AmebaArtifact aa = artifactProvider.manifest(bi)
        if (!checkIfExists || aa.location.exists()) {
            releaseConf.manifestFiles.put(bi.id, aa)
        } else {
            l.lifecycle("Skipping preparing manifest artifact for ${bi.id} : ${aa.location} -> missing")
        }
        aa
    }

    private void prepareMobileProvisionFile(IOSBuilderInfo bi) {
        AmebaArtifact aa = prepareMobileProvisionArtifact(bi)
        aa.location.parentFile.mkdirs()
        aa.location.delete()
        aa.location << bi.mobileprovision.text
        l.lifecycle("Mobile provision file created: ${aa}")
    }

    private AmebaArtifact prepareMobileProvisionArtifact(IOSBuilderInfo bi, boolean checkIfExists = false) {
        AmebaArtifact aa = artifactProvider.mobileprovision(bi)
        if (!checkIfExists || aa.location.exists()) {
            releaseConf.mobileProvisionFiles.put(bi.id, aa)
        } else {
            l.lifecycle("Skipping preparing mobileProvision artifact for ${bi.id} : ${aa.location} -> missing")
        }
        aa
    }

    void buildArtifactsOnly(AbstractIOSVariant variant) {
        def bi = artifactProvider.builderInfo(variant)
        prepareDistributionZipArtifact(bi, true)
        prepareDSYMZipArtifact(bi, true)
//            prepareAhSYMFiles(bi, true)//TODO turn on after DI is implemented
        prepareIpaArtifact(bi, true)
        prepareManifestArtifact(bi, true)
        prepareMobileProvisionArtifact(bi, true)
    }

    private void prepareSimulatorBundleFile(IOSBuilderInfo bi, String family) {

        AmebaArtifact file = new AmebaArtifact()
        file.name = "Simulator build for ${family}"
        file.url = new URL(releaseConf.baseURL, "${getFolderPrefix(bi)}/${bi.filePrefix}-${family}-simulator-image.dmg")
        file.location = new File(releaseConf.otaDir, "${getFolderPrefix(bi)}/${bi.filePrefix}-${family}-simulator-image.dmg")
        file.location.parentFile.mkdirs()
        file.location.delete()
        def File tmpDir = File.createTempFile("${conf.projectName.value}-${bi.target}-${family}-simulator", ".tmp")
        tmpDir.delete()
        tmpDir.mkdir()
        def destDir = new File(tmpDir, "${bi.target} (${family}_Simulator) ${conf.versionString}_${conf.versionCode}.app")
        destDir.mkdir()
        rsyncTemplatePreservingExecutableFlag(destDir)
        File embedDir = new File(destDir, "Contents/Resources/EmbeddedApp")
        embedDir.mkdirs()
        File sourceApp = new File(bi.buildDir, "${bi.target}.app")
        rsyncEmbeddedAppPreservingExecutableFlag(sourceApp, embedDir)
        updateBundleId(bi, destDir)
        resampleIcon(destDir)
        updateDeviceFamily(family, embedDir, bi)
        updateVersions(embedDir, bi)
        String[] cmd = [
                'hdiutil',
                'create',
                file.location.canonicalPath,
                '-srcfolder',
                destDir,
                '-volname',
                "${conf.projectName.value}-${bi.target}-${family}"
        ]
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: cmd))
        releaseConf.dmgImageFiles.put("${family}-${variantsConf.mainVariant.target}" as String, file)
        l.lifecycle("Simulator zip file created: ${file} for ${family}-${variantsConf.mainVariant.target}")
    }

    String getFolderPrefix(IOSBuilderInfo bi) {
        "${releaseConf.projectDirName}/${conf.fullVersionString}/${bi.target}/${bi.configuration}"
    }

    private rsyncTemplatePreservingExecutableFlag(File destDir) {
        def cmd = [
                'rsync',
                '-aE',
                '--exclude',
                'Contents/Resources/EmbeddedApp',
                '/Applications/Simulator Bundler.app/Contents/Resources/Launcher.app/',
                destDir
        ]
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: cmd))
    }

    private rsyncEmbeddedAppPreservingExecutableFlag(File sourceAppDir, File embedDir) {
        def cmd = [
                'rsync',
                '-aE',
                sourceAppDir,
                embedDir
        ]
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: cmd))
    }

    private updateBundleId(IOSBuilderInfo bi, File tmpDir) {
        def bundleId = mpParser.bundleId(bi.mobileprovision)
        File contentsPlist = new File(tmpDir, "Contents/Info.plist")
        runPlistBuddy("Set :CFBundleIdentifier ${bundleId}.launchsim", contentsPlist)
    }

    private resampleIcon(File tmpDir) {
        String[] cmd = [
                '/opt/local/bin/convert',
                releaseConf.iconFile.value.canonicalPath,
                '-resample',
                '128x128',
                new File(tmpDir, "Contents/Resources/Launcher.icns").canonicalPath
        ]
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: cmd))
    }

    private updateDeviceFamily(String device, File embedDir, IOSBuilderInfo bi) {
        File targetPlistFile = new File(embedDir, "${bi.target}.app/Info.plist")
        runPlistBuddy('Delete UIDeviceFamily', targetPlistFile, false)
        runPlistBuddy('Add UIDeviceFamily array', targetPlistFile)
        String family = (device == "iPhone" ? "1" : "2")
        runPlistBuddy("Add UIDeviceFamily:0 integer ${family}", targetPlistFile)
    }

    private updateVersions(File embedDir, IOSBuilderInfo bi) {
        File targetPlistFile = new File(embedDir, "${bi.target}.app/Info.plist")
        runPlistBuddy('Delete CFBundleVersion', targetPlistFile, false)
        runPlistBuddy("Add CFBundleVersion string ${conf.versionCode}", targetPlistFile)
        runPlistBuddy('Delete CFBundleShortVersionString', targetPlistFile, false)
        runPlistBuddy("Add CFBundleShortVersionString string ${conf.versionString}", targetPlistFile)
    }

    private runPlistBuddy(String command, File targetPlistFile, boolean failOnError = true) {
        String[] cmd = [
                '/usr/libexec/PlistBuddy',
                '-c',
                command,
                targetPlistFile
        ]
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: cmd, failOnError: failOnError))
    }
}
