package com.apphance.flow.plugins.android.buildplugin.tasks

import com.apphance.flow.TestUtils
import com.apphance.flow.executor.AndroidExecutor
import com.apphance.flow.util.FlowUtils
import spock.lang.Specification

@Mixin([TestUtils, FlowUtils])
class AndroidProjectUpdaterSpec extends Specification {

    def updater = new AndroidProjectUpdater()

    def 'test create src dir'() {
        given:
        def tmp = temporaryDir
        assert !new File(tmp, 'src').exists()
        updater.executor = GroovyMock(AndroidExecutor)

        when:
        updater.updateRecursively(tmp)

        then:
        assert new File(tmp, 'src').exists()
    }
}
