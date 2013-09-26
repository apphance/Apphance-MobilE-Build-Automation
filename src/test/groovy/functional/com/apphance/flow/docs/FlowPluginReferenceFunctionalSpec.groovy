package com.apphance.flow.docs

import com.apphance.flow.TestUtils
import spock.lang.Specification

@Mixin(TestUtils)
class FlowPluginReferenceFunctionalSpec extends Specification {

    def 'test generate documentation'() {
        given:
        File outHtml = tempFile
        def pluginRef = new FlowPluginReference(outputHtml:  outHtml)
        pluginRef.run()
        println outHtml.text

        expect:
        outHtml.size() > 0
        outHtml.text.contains('<font size="4">AndroidApphancePlugin</font>')
    }
}
