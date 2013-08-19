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

    private logger = getLogger(getClass())

    @Inject MobileProvisionParser mpParser

    @Lazy
    @PackageScope
    File tmplDir = {
        new File('/Applications/Simulator Bundler.app/Contents/Resources/Launcher.app/')
    }()

    @Override
    void buildArtifacts(IOSBuilderInfo bi) {
        IOSFamily.values().each {
            prepareSimulatorBundleFile(bi, it)
        }
    }

    @PackageScope
    void prepareSimulatorBundleFile(IOSBuilderInfo bi, IOSFamily family) {
        def fa = artifactProvider.simulator(bi, family)
        mkdirs(fa)

        def tmpDir = tmpDir(bi, family)
        def embedDir = embedDir(tmpDir)
        def contentsPlist = new File(tmpDir, 'Contents/Info.plist')
        def icon = new File(tmpDir, 'Contents/Resources/Launcher.icns')
        def embedPlist = new File(embedDir, "$bi.appName/Info.plist")

        syncSimAppTemplateToTmpDir(tmplDir, tmpDir)
        syncAppToTmpDir(sourceApp(bi), embedDir)

        updateBundleId(bi.mobileprovision, contentsPlist)
        resampleIcon(icon)
        updateDeviceFamily(family, embedPlist)

        createSimAppDmg(fa.location, tmpDir, "$bi.appName-${family.iFormat()}")

        releaseConf.dmgImageFiles.put("${family.iFormat()}-$bi.id" as String, fa)
        logger.info("Simulator zip file created: $fa.location")
    }

    @PackageScope
    File tmpDir(IOSBuilderInfo bi, IOSFamily family) {
        def tmpDir = createTempDir()
        tmpDir.deleteOnExit()
        def appDir = new File(tmpDir, "$bi.productName (${family.iFormat()}_Simulator) ${conf.fullVersionString}.app")
        appDir.mkdirs()
        logger.info("Temp dir for iOS simulator artifact created: $tmpDir.absolutePath")
        appDir
    }

    private File embedDir(File tmpDir) {
        def embedDir = new File(tmpDir, 'Contents/Resources/EmbeddedApp')
        embedDir.mkdirs()
        embedDir
    }

    @PackageScope
    void syncSimAppTemplateToTmpDir(File tmplDir, File tmpDir) {
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: [
                'rsync',
                '-alE',
                '--exclude',
                'Contents/Resources/EmbeddedApp',
                "$tmplDir.canonicalPath/",
                tmpDir
        ]))
    }

    @PackageScope
    File sourceApp(IOSBuilderInfo bi) {
        new File("$bi.archiveDir/Products/Applications", bi.appName)
    }

    @PackageScope
    void syncAppToTmpDir(File sourceAppDir, File embedDir) {
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: [
                'rsync', '-alE', sourceAppDir, embedDir
        ]))
    }

    @PackageScope
    void updateBundleId(File mobileprovision, File plist) {
        def bundleId = mpParser.bundleId(mobileprovision)
        runPlistBuddy("Set :CFBundleIdentifier ${bundleId}.launchsim", plist)
    }

    @PackageScope
    void resampleIcon(File icon) {
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: [
                '/opt/local/bin/convert',
                new File(conf.rootDir, releaseConf.iconFile.value.path).canonicalPath,
                '-resample',
                '128x128',
                icon.canonicalPath
        ]))
    }

    @PackageScope
    void updateDeviceFamily(IOSFamily family, File plist) {
        runPlistBuddy('Delete UIDeviceFamily', plist, false)
        runPlistBuddy('Add UIDeviceFamily array', plist)
        runPlistBuddy("Add UIDeviceFamily:0 integer $family.UIDDeviceFamily", plist)
    }

    private void runPlistBuddy(String command, File targetPlistFile, boolean failOnError = true) {
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: [
                '/usr/libexec/PlistBuddy',
                '-c',
                command,
                targetPlistFile.absolutePath
        ], failOnError: failOnError))
    }

    @PackageScope
    void createSimAppDmg(File destDir, File srcDir, String name) {
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: [
                'hdiutil',
                'create',
                destDir.canonicalPath,
                '-srcfolder',
                srcDir.canonicalPath,
                '-volname',
                name
        ]))
    }
}
