package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.util.FlowUtils

import javax.inject.Inject

import static com.google.common.io.Files.createTempDir
import static java.nio.file.Files.createSymbolicLink
import static java.nio.file.Paths.get as path

@Mixin(FlowUtils)
class FrameworkVariantTask extends AbstractBuildVariantTask {

    String description = "Prepares 'framework' file for given variant"

    @Inject IOSReleaseConfiguration releaseConf

    @Lazy
    File simTmpDir = { tempDir }()
    @Lazy
    File deviceTmpDir = { tempDir }()
    @Lazy
    File frameworkDir = { new File(tempDir, "${variant.frameworkName.value}.framework") }()
    @Lazy
    File versionsDir = { new File(frameworkDir, "Versions/${variant.frameworkVersion.value}") }()
    @Lazy
    File resourcesDir = { new File(versionsDir, 'Resources') }()
    @Lazy
    File headersDir = { new File(versionsDir, 'Headers') }()

    @Override
    void build() {
        executor.buildVariant(simTmpDir, cmdSim)
        executor.buildVariant(deviceTmpDir, cmdDevice)

        if (releaseConf.isEnabled()) {
            buildFrameworkStructure()
        }
    }

    @Lazy
    List<String> cmdSim = {
        conf.xcodebuildExecutionPath() + ['-scheme', variant.name] +
                ['-sdk', conf.simulatorSdk.value ?: 'iphonesimulator'] + ['-arch', 'i386'] +
                ["CONFIGURATION_BUILD_DIR=${simTmpDir.absolutePath}"] +
                ['clean', 'build']
    }()

    @Lazy
    List<String> cmdDevice = {
        conf.xcodebuildExecutionPath() + ['-scheme', variant.name] +
                ['-sdk', conf.sdk.value ?: 'iphoneos'] +
                ["CONFIGURATION_BUILD_DIR=${deviceTmpDir.absolutePath}"] +
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

    File getTempDir() {
        def dir = createTempDir()
        dir.deleteOnExit()
        dir
    }


}
