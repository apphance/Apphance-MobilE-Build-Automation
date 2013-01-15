package com.apphance.ameba.unit.ios

import com.apphance.ameba.ios.IOSProjectConfiguration
import org.junit.Test

class IOSProjectConfigurationTest {
    @Test
    void testSimpleToString() {
        def x = IOSProjectConfiguration[
                distributionDirectory: new java.io.File('dist'),
                targets: [
                        "target1",
                        "target2"
                ]]
        x.toString()
    }
}
