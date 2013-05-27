package com.apphance.ameba.plugins.ios.release.tasks

import com.apphance.ameba.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.plugins.ios.builder.IOSBuilderInfo
import com.apphance.ameba.plugins.ios.parsers.MobileProvisionParser
import com.apphance.ameba.plugins.release.AmebaArtifact

import javax.inject.Inject

import static com.apphance.ameba.configuration.ios.IOSConfiguration.FAMILIES
import static org.gradle.api.logging.Logging.getLogger

class IOSSimulatorArtifactsBuilder extends AbstractIOSArtifactsBuilder {

    def l = getLogger(getClass())

    @Inject IOSVariantsConfiguration variantsConf
    @Inject MobileProvisionParser mpParser

    void buildArtifacts(IOSBuilderInfo bi) {
        FAMILIES.each { family ->
            prepareSimulatorBundleFile(bi, family)
        }
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
