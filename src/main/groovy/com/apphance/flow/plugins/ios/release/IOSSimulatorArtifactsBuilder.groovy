package com.apphance.flow.plugins.ios.release

import com.apphance.flow.configuration.ios.IOSFamily
import com.apphance.flow.executor.command.Command
import com.apphance.flow.plugins.ios.builder.IOSBuilderInfo
import com.apphance.flow.plugins.ios.parsers.MobileProvisionParser

import javax.inject.Inject

import static java.io.File.createTempFile
import static org.gradle.api.logging.Logging.getLogger

class IOSSimulatorArtifactsBuilder extends AbstractIOSArtifactsBuilder {

    def logger = getLogger(getClass())

    @Inject MobileProvisionParser mpParser

    void buildArtifacts(IOSBuilderInfo bi) {
        IOSFamily.values().each {
            prepareSimulatorBundleFile(bi, it)
        }
    }

    private void prepareSimulatorBundleFile(IOSBuilderInfo bi, IOSFamily family) {
        def fa = artifactProvider.simulator(bi, family)
        mkdirs(fa)

        def destDir = destDir(bi, family)

        rsyncTemplatePreservingExecutableFlag(destDir)
        updateBundleId(bi, destDir)
        resampleIcon(destDir)

        def embedDir = embedDir(destDir)

        rsyncEmbeddedAppPreservingExecutableFlag(sourceApp(bi), embedDir)
        updateDeviceFamily(family, embedDir, bi)

        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: [
                'hdiutil',
                'create',
                fa.location.canonicalPath,
                '-srcfolder',
                destDir,
                '-volname',
                "$bi.appName-${family.iFormat()}"
        ]))
        releaseConf.dmgImageFiles.put("${family.iFormat()}-$bi.id" as String, fa)
        logger.info("Simulator zip file created: $fa.location")
        destDir.deleteDir()
    }

    private File destDir(IOSBuilderInfo bi, IOSFamily family) {
        def tmpDir = tmpDir(bi, family)
        def destDir = new File(tmpDir, "$bi.productName (${family.iFormat()}_Simulator) ${conf.versionString}_${conf.versionCode}.app")
        destDir.mkdir()
        destDir
    }

    private File tmpDir(IOSBuilderInfo bi, IOSFamily family) {
        def tmpDir = createTempFile("$bi.productName-$family-sim", '.tmp')
        tmpDir.delete()
        tmpDir.mkdir()
        tmpDir
    }

    private void rsyncTemplatePreservingExecutableFlag(File destDir) {
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: [
                'rsync',
                '-aE',
                '--exclude',
                'Contents/Resources/EmbeddedApp',
                '/Applications/Simulator Bundler.app/Contents/Resources/Launcher.app/',
                destDir
        ]))
    }

    private File embedDir(File destDir) {
        File embedDir = new File(destDir, "Contents/Resources/EmbeddedApp")
        embedDir.mkdirs()
        embedDir
    }

    private File sourceApp(IOSBuilderInfo bi) {
        new File("$bi.archiveDir/Products/Applications", bi.appName)
    }

    private void rsyncEmbeddedAppPreservingExecutableFlag(File sourceAppDir, File embedDir) {
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: [
                'rsync',
                '-aE',
                sourceAppDir,
                embedDir
        ]))
    }

    private void updateBundleId(IOSBuilderInfo bi, File tmpDir) {
        def bundleId = mpParser.bundleId(bi.mobileprovision)
        File contentsPlist = new File(tmpDir, "Contents/Info.plist")
        runPlistBuddy("Set :CFBundleIdentifier ${bundleId}.launchsim", contentsPlist)
    }

    private void resampleIcon(File tmpDir) {
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: [
                '/opt/local/bin/convert',
                new File(conf.rootDir, releaseConf.iconFile.value.path).canonicalPath,
                '-resample',
                '128x128',
                new File(tmpDir, "Contents/Resources/Launcher.icns").canonicalPath
        ]))
    }

    private void updateDeviceFamily(IOSFamily family, File embedDir, IOSBuilderInfo bi) {
        File targetPlistFile = new File(embedDir, "$bi.appName/Info.plist")
        runPlistBuddy('Delete UIDeviceFamily', targetPlistFile, false)
        runPlistBuddy('Add UIDeviceFamily array', targetPlistFile)
        runPlistBuddy("Add UIDeviceFamily:0 integer $family.UIDDeviceFamily", targetPlistFile)
    }

    private void runPlistBuddy(String command, File targetPlistFile, boolean failOnError = true) {
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: [
                '/usr/libexec/PlistBuddy',
                '-c',
                command,
                targetPlistFile
        ], failOnError: failOnError))
    }
}
