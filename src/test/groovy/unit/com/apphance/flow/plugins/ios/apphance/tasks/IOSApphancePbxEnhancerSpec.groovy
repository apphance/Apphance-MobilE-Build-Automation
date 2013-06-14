package com.apphance.flow.plugins.ios.apphance.tasks

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.executor.IOSExecutor
import spock.lang.Specification

class IOSApphancePbxEnhancerSpec extends Specification {

    def 'basic pbx json objects found'() {
        given:
        def file = new File('testProjects/ios/GradleXCode/GradleXCode.xcodeproj/project.pbxproj.json')

        and:
        def enhancer = new IOSApphancePbxEnhancer(GroovyMock(AbstractIOSVariant) {
            getTarget() >> 'GradleXCode'
            getConfiguration() >> 'BasicConfiguration'
        })
        enhancer.iosExecutor = GroovyMock(IOSExecutor) {
            getPbxProjToJSON() >> file.text.split('\n')
        }

        expect:
        enhancer.rootObject.isa == 'PBXProject'
        enhancer.target.name == 'GradleXCode'
        enhancer.configuration.name == 'BasicConfiguration'
    }
}
