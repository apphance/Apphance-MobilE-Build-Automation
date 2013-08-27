package com.apphance.flow.plugins.ios.release.artifact.builder

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.plugins.ios.release.artifact.info.IOSArtifactProvider
import com.apphance.flow.plugins.ios.release.artifact.info.IOSFrameworkArtifactInfo
import com.apphance.flow.plugins.release.FlowArtifact
import com.apphance.flow.util.FlowUtils
import spock.lang.Shared
import spock.lang.Specification

import static java.nio.file.Files.isSymbolicLink
import static java.nio.file.Files.readSymbolicLink
import static java.nio.file.Paths.get

@Mixin(FlowUtils)
class IOSFrameworkArtifactsBuilderSpec extends Specification {

    @Shared
    def info = new IOSFrameworkArtifactInfo(
            id: 'SomeId',
            frameworkName: 'Some',
            simLib: GroovyMock(File) { getAbsolutePath() >> 'sim.lib' },
            deviceLib: GroovyMock(File) { getAbsolutePath() >> 'device.lib' }
    )

    def 'framework dir structure is created'() {
        given:
        def builder = new IOSFrameworkArtifactsBuilder()

        when:
        builder.buildFrameworkStructure(info)

        then:
        def dir = info.frameworkDir
        isSymbolicLink(get(new File(dir, 'Headers').toURI()))
        isSymbolicLink(get(new File(dir, 'Resources').toURI()))
        isSymbolicLink(get(new File(dir, info.frameworkName).toURI()))
        isSymbolicLink(get(new File(dir, 'Versions/Current').toURI()))

        and:
        readSymbolicLink(get(new File(dir, 'Headers').toURI())).toString() == 'Versions/Current/Headers'
        readSymbolicLink(get(new File(dir, 'Resources').toURI())).toString() == 'Versions/Current/Resources'
        readSymbolicLink(get(new File(dir, info.frameworkName).toURI())).toString() ==
                "Versions/Current/$info.frameworkName"
        readSymbolicLink(get(new File(dir, 'Versions/Current').toURI())).toString() == 'A'

        and:
        ['Headers', 'Resources'].every {
            new File(info.frameworkDir, "Versions/A/$it/README").text ==
                    'This file is here because git ignores empty folders and some symlink points to this folder'
        }
    }

    def 'libraries are linked'() {
        given:
        def builder = new IOSFrameworkArtifactsBuilder(
                conf: GroovyMock(IOSConfiguration) { getRootDir() >> GroovyMock(File) },
                executor: GroovyMock(CommandExecutor)
        )

        when:
        builder.linkLibraries(info)

        then:
        1 * builder.executor.executeCommand({
            def cmd = it.commandForExecution.join(' ')
            cmd.startsWith('lipo -create device.lib sim.lib -output') && cmd.endsWith('Some.framework/Versions/Current/Some')
        })
    }

    def 'framework zip is created'() {
        given:
        def location = tempFile

        and:
        def builder = new IOSFrameworkArtifactsBuilder(
                executor: GroovyMock(CommandExecutor),
                artifactProvider: GroovyMock(IOSArtifactProvider) {
                    framework(_) >> GroovyMock(FlowArtifact) {
                        getLocation() >> location
                    }
                },
                releaseConf: new IOSReleaseConfiguration()
        )

        when:
        builder.prepareFrameworkZip(info)

        then:
        1 * builder.executor.executeCommand({
            def cmd = it.commandForExecution.join(' ')
            cmd.startsWith('zip -r -y') && cmd.endsWith('Some.framework')
        })
        builder.releaseConf.frameworkFiles.size() > 0
        builder.releaseConf.frameworkFiles[info.id] != null
    }
}
