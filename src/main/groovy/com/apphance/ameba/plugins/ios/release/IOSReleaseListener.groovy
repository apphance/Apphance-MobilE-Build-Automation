package com.apphance.ameba.plugins.ios.release

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.ios.IOSReleaseConfiguration
import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.executor.jython.JythonExecutor
import com.apphance.ameba.plugins.ios.IOSBuilderInfo
import com.apphance.ameba.plugins.ios.IOSXCodeOutputParser
import com.apphance.ameba.plugins.ios.MPParser
import com.apphance.ameba.plugins.ios.buildplugin.IOSBuildListener
import com.apphance.ameba.plugins.ios.buildplugin.IOSSingleVariantBuilder
import com.apphance.ameba.plugins.release.AmebaArtifact
import groovy.text.SimpleTemplateEngine
import org.gradle.api.AntBuilder
import org.gradle.api.Project

import static org.gradle.api.logging.Logging.getLogger

/**
 * Build listener for releases.
 *
 */
class IOSReleaseListener implements IOSBuildListener {

    def l = getLogger(getClass())

    CommandExecutor executor
    IOSExecutor iosExecutor
    IOSConfiguration conf
    IOSReleaseConfiguration releaseConf
    AntBuilder ant
    IOSXCodeOutputParser parser = new IOSXCodeOutputParser()

    IOSReleaseListener(Project project, IOSConfiguration conf, IOSReleaseConfiguration releaseConf, CommandExecutor executor, IOSExecutor iosExecutor) {
        this.conf = conf
        this.releaseConf = releaseConf
        this.executor = executor
        this.iosExecutor = iosExecutor
        this.ant = project.ant
    }

    @Override
    public void buildDone(Project project, IOSBuilderInfo bi) {
        if (conf.versionString != null) {
            if (bi.configuration != 'Debug') {
                prepareDistributionZipFile(project, bi)
                prepareDSYMZipFile(bi)
//                prepareAhSYMFiles(bi) //TODO turn on after DI is implemented
                prepareIpaFile(project, bi)
                prepareManifestFile(bi)
                prepareMobileProvisionFile(bi)
            } else {
                conf.families.each { family ->
                    prepareSimulatorBundleFile(project, bi, family)
                }
            }
        } else {
            l.lifecycle('Skipping building artifacts -> the build is not versioned')
        }
    }

    private void prepareDistributionZipFile(Project project, IOSBuilderInfo bi) {
        AmebaArtifact distributionZipArtifact = prepareDistributionZipArtifact(bi)
        distributionZipArtifact.location.parentFile.mkdirs()
        distributionZipArtifact.location.delete()
        ant.zip(destfile: distributionZipArtifact.location) {
            zipfileset(dir: conf.distributionDir,
                    includes: parser.findMobileProvisionFile(project, bi.target, bi.configuration).name)
            zipfileset(dir: bi.buildDir, includes: "${bi.target}.app/**")
        }
        l.lifecycle("Distribution zip file created: ${distributionZipArtifact}")
    }

    private AmebaArtifact prepareDistributionZipArtifact(IOSBuilderInfo bi, boolean checkIfExists = false) {
        AmebaArtifact distributionZipArtifact = new AmebaArtifact(
                name: 'Distribution zip',
                url: new URL(releaseConf.baseURL, "${getFolderPrefix(bi)}/${bi.filePrefix}.zip"),
                location: new File(releaseConf.otaDir, "${getFolderPrefix(bi)}/${bi.filePrefix}.zip"))
        if (!checkIfExists || distributionZipArtifact.location.exists()) {
            releaseConf.distributionZipFiles.put(bi.id, distributionZipArtifact)
        } else {
            l.lifecycle("Skipping preparing distribution zip for ${bi} -> missing")
        }
        return distributionZipArtifact
    }

    private void prepareDSYMZipFile(IOSBuilderInfo bi) {
        AmebaArtifact dSYMZipArtifact = prepareDSYMZipArtifact(bi)
        dSYMZipArtifact.location.parentFile.mkdirs()
        dSYMZipArtifact.location.delete()
        ant.zip(destfile: dSYMZipArtifact.location) {
            zipfileset(dir: bi.buildDir, includes: "${bi.target}.app.dSYM/**")
        }
        l.lifecycle("dSYM zip file created: ${dSYMZipArtifact}")
    }


    private AmebaArtifact prepareDSYMZipArtifact(IOSBuilderInfo bi, boolean checkIfExists = false) {
        AmebaArtifact dSYMZipArtifact = new AmebaArtifact(
                name: "dSYM zip",
                url: new URL(releaseConf.baseURL, "${getFolderPrefix(bi)}/${bi.filePrefix}_dSYM.zip"),
                location: new File(releaseConf.otaDir, "${getFolderPrefix(bi)}/${bi.filePrefix}_dSYM.zip"))
        if (!checkIfExists || dSYMZipArtifact.location.exists()) {
            releaseConf.dSYMZipFiles.put(bi.id, dSYMZipArtifact)
        } else {
            l.lifecycle("Skipping preparing dSYM artifact for ${bi.id} : ${dSYMZipArtifact.location} -> missing")
        }
        return dSYMZipArtifact
    }

    private void prepareAhSYMFiles(IOSBuilderInfo bi, boolean checkIfExists = false) {
        AmebaArtifact ahSYM = prepareAhSymArtifacts(bi, checkIfExists)
        ahSYM.location.delete()
        ahSYM.location.mkdirs()
        def je = new JythonExecutor()
        def args = ['-p', conf.plistFile.canonicalPath, '-d', new File(bi.buildDir, "${bi.target}.app.dSYM").canonicalPath, '-o', new File(ahSYM.location.canonicalPath, bi.target).canonicalPath]
        je.executeScript('jython/dump_reduce3_ameba.py', args)
        l.lifecycle("ahSYM files created: ${ahSYM.location.list()}")
    }

    private AmebaArtifact prepareAhSymArtifacts(IOSBuilderInfo bi, boolean checkIfExists = false) {
        AmebaArtifact aa = new AmebaArtifact(
                name: "ahSYM dir",
                url: new URL(releaseConf.baseURL, "${getFolderPrefix(bi)}/${bi.filePrefix}_ahSYM"),
                location: new File(releaseConf.otaDir, "${getFolderPrefix(bi)}/${bi.filePrefix}_ahSYM")
        )
        if (!checkIfExists || aa.location.exists()) {
            releaseConf.ahSYMDirs.put(bi.id, aa)
        } else {
            l.lifecycle("Skipping preparing dSYM artifact for ${bi.id} : ${aa.location} -> missing")
        }
        aa
    }

    private void prepareIpaFile(Project project, IOSBuilderInfo bi) {
        AmebaArtifact ipaArtifact = prepareIpaArtifact(bi)
        ipaArtifact.location.parentFile.mkdirs()
        ipaArtifact.location.delete()
        def appList = bi.buildDir.list([accept: { d, f -> f ==~ /.*\.app/ }] as FilenameFilter)
        def cmd = [
                '/usr/bin/xcrun',
                '-sdk',
                conf.sdk.value,
                'PackageApplication',
                '-v',
                new File(bi.buildDir, appList[0]).canonicalPath,
                '-o',
                ipaArtifact.location.canonicalPath,
                '--embed',
                bi.mobileProvisionFile.canonicalPath
        ]
        executor.executeCommand(new Command(runDir: project.rootDir, cmd: cmd))
        l.lifecycle("ipa file created: ${ipaArtifact}")
    }

    private AmebaArtifact prepareIpaArtifact(IOSBuilderInfo bi, boolean checkIfExists = false) {
        AmebaArtifact ipaArtifact = new AmebaArtifact(
                name: "The ipa file",
                url: new URL(releaseConf.baseURL, "${getFolderPrefix(bi)}/${bi.filePrefix}.ipa"),
                location: new File(releaseConf.otaDir, "${getFolderPrefix(bi)}/${bi.filePrefix}.ipa"))
        if (!checkIfExists || ipaArtifact.location.exists()) {
            releaseConf.ipaFiles.put(bi.id, ipaArtifact)
        } else {
            l.lifecycle("Skipping preparing ipa artifact for ${bi.id} : ${ipaArtifact.location} -> missing")
        }
        return ipaArtifact
    }


    private void prepareManifestFile(IOSBuilderInfo bi) {
        AmebaArtifact manifestArtifact = prepareManifestArtifact(bi)
        manifestArtifact.location.parentFile.mkdirs()
        manifestArtifact.location.delete()

        URL manifestTemplate = this.class.getResource("manifest.plist")
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        def bundleId = MPParser.readBundleIdFromPlist(bi.plistFile.toURI().toURL())
        def binding = [
                ipaUrl: releaseConf.ipaFiles.get(bi.id).url,
                title: bi.target,
                bundleId: bundleId
        ]
        l.lifecycle("Building manifest from ${bi.plistFile}, bundleId: ${bundleId}")
        def result = engine.createTemplate(manifestTemplate).make(binding)
        manifestArtifact.location << (result.toString())
        l.lifecycle("Manifest file created: ${manifestArtifact}")
    }


    private AmebaArtifact prepareManifestArtifact(IOSBuilderInfo bi, boolean checkIfExists = false) {
        AmebaArtifact manifestArtifact = new AmebaArtifact(
                name: "The manifest file",
                url: new URL(releaseConf.baseURL, "${getFolderPrefix(bi)}/manifest.plist"),
                location: new File(releaseConf.otaDir, "${getFolderPrefix(bi)}/manifest.plist"))
        if (!checkIfExists || manifestArtifact.location.exists()) {
            releaseConf.manifestFiles.put(bi.id, manifestArtifact)
        } else {
            l.lifecycle("Skipping preparing manifest artifact for ${bi.id} : ${manifestArtifact.location} -> missing")
        }
        return manifestArtifact
    }


    private void prepareMobileProvisionFile(IOSBuilderInfo bi) {
        AmebaArtifact mobileProvisionArtifact = prepareMobileProvisionArtifact(bi)
        mobileProvisionArtifact.location.parentFile.mkdirs()
        mobileProvisionArtifact.location.delete()
        mobileProvisionArtifact.location << bi.mobileProvisionFile.text
        l.lifecycle("Mobile provision file created: ${mobileProvisionArtifact}")
    }

    private AmebaArtifact prepareMobileProvisionArtifact(IOSBuilderInfo bi, boolean checkIfExists = false) {
        AmebaArtifact mobileProvisionArtifact = new AmebaArtifact(
                name: "The mobile provision file",
                url: new URL(releaseConf.baseURL, "${getFolderPrefix(bi)}/${bi.filePrefix}.mobileprovision"),
                location: new File(releaseConf.otaDir, "${getFolderPrefix(bi)}/${bi.filePrefix}.mobileprovision"))
        if (!checkIfExists || mobileProvisionArtifact.location.exists()) {
            releaseConf.mobileProvisionFiles.put(bi.id, mobileProvisionArtifact)
        } else {
            l.lifecycle("Skipping preparing mobileProvision artifact for ${bi.id} : ${mobileProvisionArtifact.location} -> missing")
        }
        return mobileProvisionArtifact
    }

    void buildArtifactsOnly(Project project, String target, String configuration) {
        if (conf.versionString != null) {
            IOSSingleVariantBuilder builder = new IOSSingleVariantBuilder(project, iosExecutor)
            IOSBuilderInfo bi = builder.buildSingleBuilderInfo(target, configuration, 'iphoneos', project)
            prepareDistributionZipArtifact(bi, true)
            prepareDSYMZipArtifact(bi, true)
//            prepareAhSYMFiles(bi, true)//TODO turn on after DI is implemented
            prepareIpaArtifact(bi, true)
            prepareManifestArtifact(bi, true)
            prepareMobileProvisionArtifact(bi, true)
        } else {
            l.lifecycle("Skipping building artifacts -> the build is not versioned")
        }
    }

    void prepareSimulatorBundleFile(Project project, IOSBuilderInfo bi, String family) {
        AmebaArtifact file = new AmebaArtifact()
        file.name = "Simulator build for ${family}"
        file.url = new URL(releaseConf.baseURL, "${getFolderPrefix(bi)}/${bi.filePrefix}-${family}-simulator-image.dmg")
        file.location = new File(releaseConf.otaDir, "${getFolderPrefix(bi)}/${bi.filePrefix}-${family}-simulator-image.dmg")
        file.location.parentFile.mkdirs()
        file.location.delete()
        def File tmpDir = File.createTempFile("${conf.projectName}-${bi.target}-${family}-simulator", ".tmp")
        tmpDir.delete()
        tmpDir.mkdir()
        def destDir = new File(tmpDir, "${bi.target} (${family}_Simulator) ${conf.versionString}_${conf.versionCode}.app")
        destDir.mkdir()
        rsyncTemplatePreservingExecutableFlag(project, destDir)
        File embedDir = new File(destDir, "Contents/Resources/EmbeddedApp")
        embedDir.mkdirs()
        File sourceApp = new File(bi.buildDir, "${bi.target}.app")
        rsyncEmbeddedAppPreservingExecutableFlag(project, sourceApp, embedDir)
        updateBundleId(project, bi, destDir)
        resampleIcon(project, destDir)
        updateDeviceFamily(project, family, embedDir, bi)
        updateVersions(project, embedDir, bi)
        String[] cmd = [
                'hdiutil',
                'create',
                file.location.canonicalPath,
                '-srcfolder',
                destDir,
                '-volname',
                "${conf.projectName}-${bi.target}-${family}"
        ]
        executor.executeCommand(new Command(runDir: project.rootDir, cmd: cmd))
        releaseConf.dmgImageFiles.put("${family}-${conf.mainTarget}" as String, file)
        l.lifecycle("Simulator zip file created: ${file} for ${family}-${conf.mainTarget}")
    }

    String getFolderPrefix(IOSBuilderInfo bi) {
        "${releaseConf.projectDirName}/${conf.fullVersionString}/${bi.target}/${bi.configuration}"
    }

    private rsyncTemplatePreservingExecutableFlag(Project project, File destDir) {
        def cmd = [
                'rsync',
                '-aE',
                '--exclude',
                'Contents/Resources/EmbeddedApp',
                '/Applications/Simulator Bundler.app/Contents/Resources/Launcher.app/',
                destDir
        ]
        executor.executeCommand(new Command(runDir: project.rootDir, cmd: cmd))
    }

    private rsyncEmbeddedAppPreservingExecutableFlag(Project project, File sourceAppDir, File embedDir) {
        def cmd = [
                'rsync',
                '-aE',
                sourceAppDir,
                embedDir
        ]
        executor.executeCommand(new Command(runDir: project.rootDir, cmd: cmd))
    }

    private updateBundleId(Project project, IOSBuilderInfo bi, File tmpDir) {
        def bundleId = MPParser.readBundleIdFromProvisionFile(bi.mobileProvisionFile.toURI().toURL())
        File contentsPlist = new File(tmpDir, "Contents/Info.plist")
        runPlistBuddy(project, "Set :CFBundleIdentifier ${bundleId}.launchsim", contentsPlist)
    }

    private resampleIcon(Project project, File tmpDir) {
        String[] cmd = [
                '/opt/local/bin/convert',
                releaseConf.iconFile.value.canonicalPath,
                '-resample',
                '128x128',
                new File(tmpDir, "Contents/Resources/Launcher.icns").canonicalPath
        ]
        executor.executeCommand(new Command(runDir: project.rootDir, cmd: cmd))
    }

    private updateDeviceFamily(Project project, String device, File embedDir, IOSBuilderInfo bi) {
        File targetPlistFile = new File(embedDir, "${bi.target}.app/Info.plist")
        runPlistBuddy(project, 'Delete UIDeviceFamily', targetPlistFile, false)
        runPlistBuddy(project, 'Add UIDeviceFamily array', targetPlistFile)
        String family = (device == "iPhone" ? "1" : "2")
        runPlistBuddy(project, "Add UIDeviceFamily:0 integer ${family}", targetPlistFile)
    }

    private updateVersions(Project project, File embedDir, IOSBuilderInfo bi) {
        File targetPlistFile = new File(embedDir, "${bi.target}.app/Info.plist")
        runPlistBuddy(project, 'Delete CFBundleVersion', targetPlistFile, false)
        runPlistBuddy(project, "Add CFBundleVersion string ${conf.versionCode}", targetPlistFile)
        runPlistBuddy(project, 'Delete CFBundleShortVersionString', targetPlistFile, false)
        runPlistBuddy(project, "Add CFBundleShortVersionString string ${conf.versionString}", targetPlistFile)
    }

    private runPlistBuddy(Project project, String command, File targetPlistFile, boolean failOnError = true) {
        String[] cmd = [
                '/usr/libexec/PlistBuddy',
                '-c',
                command,
                targetPlistFile
        ]
        executor.executeCommand(new Command(runDir: project.rootDir, cmd: cmd, failOnError: failOnError))
    }
}
