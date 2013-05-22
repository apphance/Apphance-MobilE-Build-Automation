package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.ios.variants.IOSSchemeVariant
import com.apphance.ameba.configuration.ios.variants.IOSVariantsConfiguration
import com.google.common.io.Files
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder

class CopySourcesTaskSpec extends Specification {

    def 'sources are copied to appropriate dirs'() {
        given:
        def p = builder().withProjectDir(new File('testProjects/ios/GradleXCode')).build()

        and:
        def tmpDir = Files.createTempDir()

        and:
        def v1 = GroovyMock(IOSSchemeVariant)
        v1.tmpDir >> new File(tmpDir, 'v1')
        def v2 = GroovyMock(IOSSchemeVariant)
        v2.tmpDir >> new File(tmpDir, 'v2')

        and:
        def variantsConf = GroovyMock(IOSVariantsConfiguration)
        variantsConf.variants >> [v1, v2]

        and:
        def conf = GroovySpy(IOSConfiguration)
        conf.project = p
        conf.sourceExcludes >> []

        and:
        def task = p.task(CopySourcesTask.NAME, type: CopySourcesTask) as CopySourcesTask
        task.conf = conf
        task.variantsConf = variantsConf

        when:
        task.copySources()

        then:
        def v1dir = new File(tmpDir, 'v1')
        v1dir.exists()
        v1dir.isDirectory()
        v1dir.list().size() > 0
        v1dir.listFiles().contains(new File(v1dir, 'GradleXCode'))
        def v2dir = new File(tmpDir, 'v2')
        v2dir.exists()
        v2dir.isDirectory()
        v2dir.list().size() > 0
        v2dir.listFiles().contains(new File(v2dir, 'GradleXCode'))

        cleanup:
        tmpDir.deleteDir()
    }
}
