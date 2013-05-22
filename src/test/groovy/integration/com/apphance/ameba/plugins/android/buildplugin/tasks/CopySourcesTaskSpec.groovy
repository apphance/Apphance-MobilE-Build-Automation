package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.ameba.configuration.android.variants.AndroidVariantsConfiguration
import org.gradle.api.Project
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder

class CopySourcesTaskSpec extends Specification {

    def 'resources are copied'() {
        given:
        def p = builder().withProjectDir(new File('testProjects/android/android-basic')).build()

        and:
        def conf = GroovySpy(AndroidConfiguration, {
            getSourceExcludes() >> []
        })
        conf.project = GroovyStub(Project) {
            getRootDir() >> p.rootDir
            file('ameba-tmp') >> p.file('ameba-tmp')
        }

        and:
        def releaseConf = GroovyMock(AndroidReleaseConfiguration, {
            getOtaDir() >> p.file('ameba-ota')
        })

        and:
        def variantsConf = GroovyMock(AndroidVariantsConfiguration) {
            getVariants() >> [
                    GroovyMock(AndroidVariantConfiguration) {
                        getTmpDir() >> p.file('ameba-tmp/market')
                    },
                    GroovyMock(AndroidVariantConfiguration) {
                        getTmpDir() >> p.file('ameba-tmp/test')
                    }]
            getVariantsDir() >> p.file('variants')
        }

        and:
        def task = p.task(CopySourcesTask.NAME, type: CopySourcesTask) as CopySourcesTask
        task.conf = conf
        task.releaseConf = releaseConf
        task.variantsConf = variantsConf

        and:
        p.file('ameba-tmp').deleteDir()

        expect:
        !p.file('ameba-tmp').exists()

        when:
        task.copySources()

        then:
        def marketDir = p.file('ameba-tmp/market')
        def testDir = p.file('ameba-tmp/test')
        marketDir.exists() && marketDir.isDirectory() && marketDir.list().size() > 0
        testDir.exists() && testDir.isDirectory() && testDir.list().size() > 0

        !(new File(marketDir, 'variants')).exists()
        !(new File(testDir, 'variants')).exists()
        !(new File(marketDir, 'ameba-tmp')).exists()
        !(new File(marketDir, 'ameba-tmp')).exists()
        !(new File(marketDir, 'ameba-ota')).exists()
        !(new File(marketDir, 'ameba-ota')).exists()
        !(new File(testDir, 'log')).exists()
        !(new File(testDir, 'log')).exists()
    }
}
