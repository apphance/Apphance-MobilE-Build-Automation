package com.apphance.ameba.plugins.android.builder

import com.apphance.ameba.TestUtils
import com.apphance.ameba.configuration.android.AndroidBuildMode
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.apphance.ApphanceConfiguration
import com.apphance.ameba.executor.AntExecutor
import com.apphance.ameba.plugins.android.buildplugin.tasks.SingleVariantTask
import com.apphance.ameba.plugins.release.AmebaArtifact
import org.gradle.api.AntBuilder as AntBuilder
import spock.lang.Specification

import static com.apphance.ameba.executor.AntExecutor.CLEAN
import static com.google.common.io.Files.createTempDir

@Mixin(TestUtils)
class SingleVariantTaskUnitSpec extends Specification {

    def task = create SingleVariantTask
    def tempDir = createTempDir()
    AndroidBuilderInfo builderInfo

    def setup() {
        builderInfo = GroovyStub(AndroidBuilderInfo) {
            getTmpDir() >> tempDir
            getMode() >> AndroidBuildMode.DEBUG
            getOriginalFile() >> createTempFile()
        }
        with(task) {
            artifactProvider = GroovyStub(AndroidArtifactProvider) {
                builderInfo(_) >> builderInfo
                artifact(_) >> GroovyStub(AmebaArtifact) {
                    getLocation() >> createTempFile()
                }
            }

            androidReleaseConf = GroovyStub(AndroidReleaseConfiguration)
            apphanceConf = GroovyStub(ApphanceConfiguration)
            apphanceConf.enabled >> false

            ant = GroovyMock(AntBuilder)
            antExecutor = GroovyMock(AntExecutor)
        }
    }

    def 'test ant executor tasks'() {
        given:
        task.androidReleaseConf.enabled >> false

        when:
        task.singleVariant()

        then:
        with(task) {
            1 * antExecutor.executeTarget(tempDir, CLEAN)
            1 * antExecutor.executeTarget(tempDir, 'debug')
            0 * antExecutor.executeTarget(_, _)
            0 * ant.copy(_)
        }
    }

    def 'test override files from variant dir'() {
        given: 'variant has its directory'
        builderInfo.variantDir >> createTempDir()
        task.androidReleaseConf.enabled >> false

        when:
        task.singleVariant()

        then:
        1 * task.ant.copy(*_)
    }

    def 'test copy to ota'() {
        given: 'variant has its directory'
        task.androidReleaseConf.enabled >> true

        when:
        task.singleVariant()

        then:
        1 * task.ant.copy(*_)
    }
}
