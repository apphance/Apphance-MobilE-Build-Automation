package com.apphance.flow.plugins.ios.release.artifact.builder

import com.apphance.flow.plugins.ios.release.artifact.info.IOSFrameworkArtifactInfo
import spock.lang.Specification

import static java.nio.file.Files.isSymbolicLink
import static java.nio.file.Files.readSymbolicLink
import static java.nio.file.Paths.get

class IOSFrameworkArtifactsBuilderSpec extends Specification {

    def 'framework dir structure is created'() {
        given:
        def frameworkName = 'Some'
        def info = new IOSFrameworkArtifactInfo(
                frameworkName: frameworkName
        )
        and:
        def builder = new IOSFrameworkArtifactsBuilder()


        when:
        builder.buildFrameworkStructure(info)

        then:
        def dir = info.frameworkDir
        isSymbolicLink(get(new File(dir, 'Headers').toURI()))
        isSymbolicLink(get(new File(dir, 'Resources').toURI()))
        isSymbolicLink(get(new File(dir, frameworkName).toURI()))
        isSymbolicLink(get(new File(dir, 'Versions/Current').toURI()))
        readSymbolicLink(get(new File(dir, 'Headers').toURI())).toString() == 'Versions/Current/Headers'
        readSymbolicLink(get(new File(dir, 'Resources').toURI())).toString() == 'Versions/Current/Resources'
        readSymbolicLink(get(new File(dir, frameworkName).toURI())).toString() == "Versions/Current/$frameworkName"
        readSymbolicLink(get(new File(dir, 'Versions/Current').toURI())).toString() == 'A'
    }
}
