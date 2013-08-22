package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.configuration.properties.StringProperty
import spock.lang.Specification

import static java.nio.file.Files.isSymbolicLink
import static java.nio.file.Files.readSymbolicLink
import static java.nio.file.Paths.get
import static org.gradle.testfixtures.ProjectBuilder.builder

class FrameworkVariantTaskSpec extends Specification {

    def project = builder().build()
    def task = project.task('frameworkTask', type: FrameworkVariantTask) as FrameworkVariantTask

    def 'framework dir structure is created'() {
        given:
        def frameworkName = 'Some'
        task.variant = GroovyMock(IOSVariant) {
            getFrameworkName() >> new StringProperty(value: frameworkName)
            getFrameworkVersion() >> new StringProperty(value: 'A')
        }

        when:
        task.buildFrameworkStructure()

        then:
        def dir = task.frameworkDir
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
