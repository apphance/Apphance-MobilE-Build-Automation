package com.apphance.flow.plugins.ios.release.artifact.builder

import com.apphance.flow.executor.command.Command
import com.apphance.flow.plugins.ios.release.artifact.info.IOSFrameworkArtifactInfo
import com.google.common.io.Files
import groovy.transform.PackageScope

import static java.nio.file.Files.createSymbolicLink
import static java.nio.file.Paths.get as path
import static org.gradle.api.logging.Logging.getLogger

class IOSFrameworkArtifactsBuilder extends AbstractIOSArtifactsBuilder<IOSFrameworkArtifactInfo> {

    private logger = getLogger(getClass())

    @Override
    void buildArtifacts(IOSFrameworkArtifactInfo info) {
        buildFrameworkStructure(info)
        linkLibraries(info)
        copyHeaders(info)
        copyResources(info)
        prepareFrameworkZip(info)
    }

    @PackageScope
    void buildFrameworkStructure(IOSFrameworkArtifactInfo info) {
        logger.info("Temp framework dir: $info.frameworkDir.absolutePath")
        mkdirs(info)
        createSymbolicLinks(info)
        addReadmeFileToPotentiallyEmptyDir(info.resourcesDir)
        addReadmeFileToPotentiallyEmptyDir(info.headersDir)
    }

    @PackageScope
    void mkdirs(IOSFrameworkArtifactInfo info) {
        info.frameworkDir.mkdirs()
        info.versionsDir.mkdirs()
        info.resourcesDir.mkdirs()
        info.headersDir.mkdirs()
    }

    @PackageScope
    void createSymbolicLinks(IOSFrameworkArtifactInfo info) {
        createSymbolicLink(path(new File(info.versionsDir.parent, 'Current').toURI()), path(info.versionsDir.name))
        createSymbolicLink(path(new File(info.frameworkDir, 'Headers').toURI()), path('Versions/Current/Headers'))
        createSymbolicLink(path(new File(info.frameworkDir, 'Resources').toURI()), path('Versions/Current/Resources'))
        createSymbolicLink(path(new File(info.frameworkDir, info.frameworkName).toURI()),
                path("Versions/Current/${info.frameworkName}"))
    }

    @PackageScope
    void addReadmeFileToPotentiallyEmptyDir(File dir) {
        def file = new File(dir, 'README')
        file.text = 'This file is here because git ignores empty folders and some symlink points to this folder'
    }

    @PackageScope
    void linkLibraries(IOSFrameworkArtifactInfo info) {
        executor.executeCommand(new Command(
                runDir: conf.rootDir,
                cmd: ['lipo', '-create', info.deviceLib.absolutePath, info.simLib.absolutePath,
                        '-output', new File(info.frameworkDir, "Versions/Current/${info.frameworkName}")
                ]))
    }

    @PackageScope
    void copyHeaders(IOSFrameworkArtifactInfo info) {
        info.headers?.each(copy.curry(info.variantDir, info.headersDir))
    }

    @PackageScope
    void copyResources(IOSFrameworkArtifactInfo info) {
        info.resources?.each(copy.curry(info.variantDir, info.resourcesDir))
    }

    @PackageScope
    Closure copy = { File variantDir, File sourceDir, String path ->
        def toCopy = new File(path)
        def source = new File(variantDir, path)
        def target = new File(sourceDir, toCopy.name)
        logger.info("Copying ${source.absolutePath} to ${target.absolutePath}")
        Files.copy(source, target)
    }

    @PackageScope
    void prepareFrameworkZip(IOSFrameworkArtifactInfo info) {
        def fa = artifactProvider.framework(info)
        releaseConf.frameworkFiles.put(info.id, fa)
        mkdirs(fa)
        executor.executeCommand(new Command(
                runDir: new File(info.frameworkDir.parent),
                cmd: ['zip', '-r', '-y', fa.location.absolutePath, info.frameworkDir.name]
        ))
        logger.info("Framework zip file created: $fa.location.absolutePath")
    }
}
