package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.properties.IOSBuildModeProperty
import com.google.common.io.Files
import spock.lang.Specification

import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.configuration.ios.IOSBuildMode.SIMULATOR
import static org.gradle.testfixtures.ProjectBuilder.builder

class CopySourcesTaskSpec extends Specification {

    def 'sources are copied to appropriate dirs'() {
        given:
        def p = builder().withProjectDir(new File('testProjects/ios/GradleXCode')).build()

        and:
        def tmpDir = Files.createTempDir()

        and:
        def variantsConf = GroovySpy(IOSVariantsConfiguration)
        variantsConf.getVariants() >> [
                GroovyStub(IOSVariant, {
                    getMode() >> new IOSBuildModeProperty(value: SIMULATOR)
                    getTmpDir() >> new File(tmpDir, 'v1')
                }),
                GroovyStub(IOSVariant, {
                    getMode() >> new IOSBuildModeProperty(value: DEVICE)
                    getTmpDir() >> new File(tmpDir, 'v2')
                })
        ]

        and:
        def conf = GroovySpy(IOSConfiguration)
        conf.project = p
        conf.sourceExcludes >> []

        and:
        def releaseConf = GroovySpy(IOSReleaseConfiguration)
        releaseConf.getOtaDir() >> p.file('flow-ota')

        and:
        def task = p.task(CopySourcesTask.NAME, type: CopySourcesTask) as CopySourcesTask
        task.conf = conf
        task.releaseConf = releaseConf
        task.variantsConf = variantsConf

        when:
        task.copySources()

        then:
        def v1dir = new File(tmpDir, 'v1')
        v1dir.exists()
        v1dir.isDirectory()
        v1dir.list().size() > 0
        v1dir.listFiles().contains(new File(v1dir, 'GradleXCode'))
        !(new File(v1dir, 'log').exists())
        !(new File(v1dir, 'flow-log').exists())
        !(new File(v1dir, 'flow-ota').exists())
        !(new File(v1dir, 'flow-tmp').exists())
        def v2dir = new File(tmpDir, 'v2')
        v2dir.exists()
        v2dir.isDirectory()
        v2dir.list().size() > 0
        v2dir.listFiles().contains(new File(v2dir, 'GradleXCode'))
        !(new File(v2dir, 'log').exists())
        !(new File(v2dir, 'flow-log').exists())
        !(new File(v2dir, 'flow-ota').exists())
        !(new File(v2dir, 'flow-tmp').exists())

        cleanup:
        tmpDir.deleteDir()
    }
}
