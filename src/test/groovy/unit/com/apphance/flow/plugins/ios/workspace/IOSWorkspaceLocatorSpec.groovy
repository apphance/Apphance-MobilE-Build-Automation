package com.apphance.flow.plugins.ios.workspace

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.util.FlowUtils
import org.gradle.api.GradleException
import spock.lang.Specification

@Mixin(FlowUtils)
class IOSWorkspaceLocatorSpec extends Specification {

    def locator = new IOSWorkspaceLocator()

    def 'workspaces are found'() {
        given:
        locator.conf = GroovyMock(IOSConfiguration) {
            getRootDir() >> new File(root)
        }

        expect:
        locator.workspaces*.toString() == workspaces

        where:
        root                             | workspaces
        'testProjects/ios/GradleXCode'   | []
        'testProjects/ios/GradleXCodeWS' | ['testProjects/ios/GradleXCodeWS/GradleXCodeWS.xcworkspace']
    }

    def 'workspace is found for given name'() {
        given:
        locator.conf = GroovyMock(IOSConfiguration) {
            getRootDir() >> new File(root)
        }
        when:
        def ws = locator.findWorkspace(name)

        then:
        ws.name == expectedName
        ws.exists() == exists

        where:
        root                             | name            | expectedName                | exists
        'testProjects/ios/GradleXCode'   | 'GradleXCodeWS' | 'GradleXCodeWS.xcworkspace' | false
        'testProjects/ios/GradleXCodeWS' | 'GradleXCodeWS' | 'GradleXCodeWS.xcworkspace' | true
    }

    def 'exception thrown when 1+ schemes found for given name'() {
        given:
        def tmpDir2 = temporaryDir
        def sDir = new File(tmpDir2, 'other')
        sDir.mkdirs()
        def s1 = new File(tmpDir2, 'GradleXCodeWS.xcworkspace')
        def s2 = new File(sDir, 'GradleXCodeWS.xcworkspace')
        s1.mkdirs()
        s2.mkdirs()
        def c1 = new File("${tmpDir2.absolutePath}/GradleXCodeWS.xcworkspace", 'contents.xcworkspacedata')
        def c2 = new File("${sDir.absolutePath}/GradleXCodeWS.xcworkspace", 'contents.xcworkspacedata')
        c1.text = '<Workspace></Workspace>'
        c2.text = '<Workspace></Workspace>'

        and:
        locator.conf = GroovyMock(IOSConfiguration) {
            getRootDir() >> tmpDir2
        }

        when:
        locator.findWorkspace('GradleXCodeWS')

        then:
        def e = thrown(GradleException)
        e.message.startsWith('Found more than one workspace file for name: GradleXCodeWS')
    }
}
