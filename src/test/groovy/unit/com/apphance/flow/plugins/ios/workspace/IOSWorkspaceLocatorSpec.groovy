package com.apphance.flow.plugins.ios.workspace

import com.apphance.flow.configuration.ios.IOSConfiguration
import spock.lang.Specification

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
}
