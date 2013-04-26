package com.apphance.ameba.plugins.ios.apphance

import com.apphance.ameba.plugins.ios.apphance.tasks.PbxProjectHelper
import spock.lang.Specification

class IOSPBXProjectHelperSpec extends Specification {

    def 'checks pbx parser output'() {
        given:
        def helper = new PbxProjectHelper()

        and:
        def pbxFile = new File(getClass().getResource('project.pbxproj').file)

        when:
        def parsedProject = helper.getParsedProject(pbxFile)
        helper.setRootObject(parsedProject)

        then:
        parsedProject
        helper.getObject('D382B70814703FE500E9CC9B')
        'D382B70B14703FE500E9CC9B' == helper.getProperty(helper.getObject('D382B70814703FE500E9CC9B'), 'buildConfigurationList').text()
        '1' == helper.getProperty(parsedProject.dict, 'archiveVersion').text()
        'D382B70814703FE500E9CC9B' == helper.getProperty(parsedProject.dict, 'rootObject').text()

        when:
        def object = helper.getObject(helper.getProperty(parsedProject.dict, 'rootObject').text())

        then:
        object

        when:
        helper.writePlistToString()

        then:
        pbxFile
    }
}
