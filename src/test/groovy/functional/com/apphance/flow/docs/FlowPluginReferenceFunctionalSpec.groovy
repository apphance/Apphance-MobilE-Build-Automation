package com.apphance.flow.docs

import com.apphance.flow.TestUtils
import spock.lang.Specification

import static org.apache.commons.io.FileUtils.deleteDirectory

@Mixin(TestUtils)
class FlowPluginReferenceFunctionalSpec extends Specification {

    def 'test generate documentation'() {
        given:
        deleteDirectory(new File('build/doc/'))
        new FlowPluginReference().run()

        expect:
        ['build/doc/confs.html', 'build/doc/plugins.html'].collect { new File(it) }.every {
            it.exists() && it.size() > 0
        }
    }
}
