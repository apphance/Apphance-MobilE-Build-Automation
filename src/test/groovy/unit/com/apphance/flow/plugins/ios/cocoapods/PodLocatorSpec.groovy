package com.apphance.flow.plugins.ios.cocoapods

import com.apphance.flow.util.FlowUtils
import spock.lang.Specification

@Mixin(FlowUtils)
class PodLocatorSpec extends Specification {

    def locator = new PodLocator()

    def 'podfile is found'() {
        given:
        def tmpDir = temporaryDir
        def tmpPod = new File(tmpDir, 'Podfile')
        tmpPod.text = "platform :ios, '6.0'"

        when:
        def pod = locator.findPodfile(tmpDir)

        then:
        pod.exists()
        pod.isFile()
        pod.size() > 0
    }

    def 'podfile is not found'() {
        expect:
        !locator.findPodfile(temporaryDir)
    }
}
