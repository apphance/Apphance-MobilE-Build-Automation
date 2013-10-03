package com.apphance.flow.plugins.android.buildplugin.tasks

import com.apphance.flow.TestUtils
import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import org.gradle.api.Project
import spock.lang.Specification

import static com.apphance.flow.configuration.ProjectConfiguration.LOG_DIR
import static com.apphance.flow.configuration.ProjectConfiguration.TMP_DIR
import static com.apphance.flow.configuration.release.ReleaseConfiguration.OTA_DIR
import static org.apache.commons.io.FileUtils.copyDirectory
import static org.gradle.testfixtures.ProjectBuilder.builder

@Mixin(TestUtils)
class CopySourcesTaskSpec extends Specification {

    def 'resources are copied'() {
        given:
        def workDir = temporaryDir
        copyDirectory(new File('projects/test/android/android-basic'), workDir)
        def p = builder().withProjectDir(workDir).build()

        and:
        def conf = GroovySpy(AndroidConfiguration, {
            getSourceExcludes() >> []
        })
        conf.project = GroovyStub(Project) {
            getRootDir() >> p.rootDir
            file(TMP_DIR) >> p.file(TMP_DIR)
            file(LOG_DIR) >> p.file(LOG_DIR)
        }

        and:
        def releaseConf = GroovyMock(AndroidReleaseConfiguration, {
            getOtaDir() >> p.file(OTA_DIR)
        })

        and:
        def variantsConf = GroovyMock(AndroidVariantsConfiguration) {
            getVariants() >> [
                    GroovyMock(AndroidVariantConfiguration) {
                        getTmpDir() >> p.file("${TMP_DIR}/market")
                    },
                    GroovyMock(AndroidVariantConfiguration) {
                        getTmpDir() >> p.file("${TMP_DIR}/test")
                    }]
            getVariantsDir() >> p.file('variants')
        }

        and:
        def task = p.task(CopySourcesTask.NAME, type: CopySourcesTask) as CopySourcesTask
        task.conf = conf
        task.releaseConf = releaseConf
        task.variantsConf = variantsConf

        and:
        p.file(TMP_DIR).deleteDir()

        expect:
        !p.file(TMP_DIR).exists()

        when:
        task.copySources()

        then:
        def marketDir = p.file("${TMP_DIR}/market")
        def testDir = p.file("${TMP_DIR}/test")
        marketDir.exists() && marketDir.isDirectory() && marketDir.list().size() > 0
        testDir.exists() && testDir.isDirectory() && testDir.list().size() > 0

        !(new File(marketDir, TMP_DIR)).exists()
        !(new File(marketDir, OTA_DIR)).exists()
        !(new File(testDir, LOG_DIR)).exists()
    }
}
