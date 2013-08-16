package com.apphance.flow.plugins.ios.release

import com.apphance.flow.configuration.ios.IOSFamily
import com.apphance.flow.executor.command.Command
import com.apphance.flow.plugins.ios.builder.IOSBuilderInfo
import com.apphance.flow.plugins.ios.parsers.MobileProvisionParser
import groovy.transform.PackageScope

import javax.inject.Inject

import static com.google.common.io.Files.createTempDir
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

        def tmpDir = this.tmpDir(bi, family)
        def tmplDir = new File(getClass().getResource('ios_sim_tmpl').toURI())

        syncSimAppTemplateToTmpDir(tmplDir, tmpDir)
        updateBundleId(bi, tmpDir)
        resampleIcon(tmpDir)

        def embedDir = embedDir(tmpDir)

        rsyncEmbeddedAppPreservingExecutableFlag(sourceApp(bi), embedDir)
        updateDeviceFamily(family, embedDir, bi)

        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: [
                'hdiutil',
                'create',
                fa.location.canonicalPath,
                '-srcfolder',
                tmpDir,
                '-volname',
                "$bi.appName-${family.iFormat()}"
        ]))
        releaseConf.dmgImageFiles.put("${family.iFormat()}-$bi.id" as String, fa)
        logger.info("Simulator zip file created: $fa.location")
        tmpDir.deleteDir()
    }

    private File tmpDir(IOSBuilderInfo bi, IOSFamily family) {
        def tmpDir = createTempDir()
        tmpDir.deleteOnExit()
        def appDir = new File(tmpDir, "$bi.productName (${family.iFormat()}_Simulator) ${conf.versionString}_${conf.versionCode}.app")
        appDir.mkdirs()
        appDir
    }

    @PackageScope
    void syncSimAppTemplateToTmpDir(File tmplDir, File tmpDir) {
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: [
                'rsync', '-alE', "${tmplDir}/", tmpDir
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
