package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.executor.command.Command
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.plugins.ios.release.artifact.builder.IOSFrameworkArtifactsBuilder
import com.apphance.flow.plugins.ios.release.artifact.info.IOSArtifactProvider
import com.google.common.io.Files

import javax.inject.Inject

import static com.google.common.io.Files.createTempDir
import static java.nio.file.Files.createSymbolicLink
import static java.nio.file.Paths.get as path


//TODO move to framework artifacts builder ??
class FrameworkVariantTask extends AbstractBuildVariantTask {

    String description = "Prepares 'framework' file for given variant"

    @Inject IOSReleaseConfiguration releaseConf
    @Inject CommandExecutor executor
    @Inject IOSFrameworkArtifactsBuilder frameworkArtifactsBuilder
    @Inject IOSArtifactProvider artifactProvider

    @Lazy
    File simTmpDir = { tempDir }()
    @Lazy
    File deviceTmpDir = { tempDir }()
    @Lazy
    File frameworkDir = { new File(tempDir, "${variant.frameworkName.value}.framework") }()
    @Lazy
    File versionsDir = { new File(frameworkDir, 'Versions/A') }()
    @Lazy
    File resourcesDir = { new File(versionsDir, 'Resources') }()
    @Lazy
    File headersDir = { new File(versionsDir, 'Headers') }()

    @Override
    void build() {
        iosExecutor.buildVariant(variant.tmpDir, cmdSim)
        iosExecutor.buildVariant(variant.tmpDir, cmdDevice)

        if (releaseConf.isEnabled()) {
            logger.info("Temp framework dir: $frameworkDir.absolutePath")
            buildFrameworkStructure()
            linkLibraries()
            copyHeaders()
            copyResources()
            def info = artifactProvider.frameworkInfo(variant)
            info.frameworkDir = frameworkDir
            frameworkArtifactsBuilder.buildArtifacts(info)
        }
    }

    @Lazy
    List<String> cmdSim = {
        conf.xcodebuildExecutionPath() + ['-scheme', variant.name] +
                ['-sdk', conf.simulatorSdk.value ?: 'iphonesimulator'] + ['-arch', 'i386'] +
                ['-configuration', variant.archiveConfiguration] +
                ["CONFIGURATION_BUILD_DIR=${simTmpDir.absolutePath}"] +
                ['PRODUCT_NAME=sim'] +
                ['clean', 'build']
    }()

    @Lazy
    List<String> cmdDevice = {
        conf.xcodebuildExecutionPath() + ['-scheme', variant.name] +
                ['-sdk', conf.sdk.value ?: 'iphoneos'] +
                ['-configuration', variant.archiveConfiguration] +
                ["CONFIGURATION_BUILD_DIR=${deviceTmpDir.absolutePath}"] +
                ['PRODUCT_NAME=device'] +
                ['clean', 'build']
    }()

    void buildFrameworkStructure() {
        mkdirs()
        createSymbolicLinks()
    }

    void mkdirs() {
        frameworkDir.mkdirs()
        versionsDir.mkdirs()
        resourcesDir.mkdirs()
        headersDir.mkdirs()
    }

    void createSymbolicLinks() {
        createSymbolicLink(path(new File(versionsDir.parent, 'Current').toURI()), path(versionsDir.name))
        createSymbolicLink(path(new File(frameworkDir, 'Headers').toURI()), path('Versions/Current/Headers'))
        createSymbolicLink(path(new File(frameworkDir, 'Resources').toURI()), path('Versions/Current/Resources'))
        createSymbolicLink(path(new File(frameworkDir, variant.frameworkName.value).toURI()),
                path("Versions/Current/${variant.frameworkName.value}"))
    }

    void copyHeaders() {
        variant.frameworkHeaders.value?.each {
            def header = new File(it)
            def source = new File(variant.tmpDir, it)
            def target = new File(headersDir, header.name)
            logger.info("Copying ${source.absolutePath} to ${target.absolutePath}")
            Files.copy(source, target)
        }
    }

    void copyResources() {
        variant.frameworkResources.value?.each {
            def resource = new File(it)
            def source = new File(variant.tmpDir, it)
            def target = new File(resourcesDir, resource.name)
            logger.info("Copying ${source.absolutePath} to ${target.absolutePath}")
            Files.copy(source, target)
        }
    }

    void linkLibraries() {
        def deviceLib = new File(deviceTmpDir, 'libdevice.a')
        def simLib = new File(simTmpDir, 'libsim.a')
        executor.executeCommand(new Command(runDir: conf.rootDir,
                cmd: ['lipo', '-create', deviceLib.absolutePath, simLib.absolutePath,
                        '-output', new File(frameworkDir, "Versions/Current/${variant.frameworkName.value}")
                ]))
    }

    File getTempDir() {
        def dir = createTempDir()
        dir.deleteOnExit()
        dir
    }
}
