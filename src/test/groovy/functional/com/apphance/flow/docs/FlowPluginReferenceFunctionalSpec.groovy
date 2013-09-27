package com.apphance.flow.docs

import com.apphance.flow.TestUtils
import spock.lang.Specification

@Mixin(TestUtils)
class FlowPluginReferenceFunctionalSpec extends Specification {

    def 'test generate documentation'() {
        given:
        def pluginRef = new FlowPluginReference()
        pluginRef.run()

        expect:
        ['build/doc/conf.html', 'build/doc/plugin.html'].every {
            def f = new File(it)
            f.exists() && f.size() > 0
        }
    }
}
