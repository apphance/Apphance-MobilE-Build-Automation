package com.apphance.flow.plugins.android.builder

import com.apphance.flow.TestUtils
import com.apphance.flow.configuration.android.AndroidBuildMode
import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.AndroidExecutor
import com.apphance.flow.executor.AntExecutor
import com.apphance.flow.plugins.android.buildplugin.tasks.SingleVariantTask
import com.apphance.flow.plugins.release.AmebaArtifact
import org.gradle.api.AntBuilder as AntBuilder
import spock.lang.Specification

import static com.apphance.flow.executor.AntExecutor.CLEAN
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

            conf = GroovyStub(AndroidConfiguration) {
                getTarget() >> new StringProperty(value: 'android-8')
                getProjectName() >> new StringProperty(value: 'TestAndroidProject')
            }
            releaseConf = GroovyStub(AndroidReleaseConfiguration)
            variant = GroovyStub(AndroidVariantConfiguration) {
                getTmpDir() >> new File('temp-variant-dir')
            }
            apphanceConf = GroovyStub(ApphanceConfiguration)
            apphanceConf.enabled >> false

            ant = GroovyMock(AntBuilder)
            antExecutor = GroovyMock(AntExecutor)
            androidExecutor = GroovyMock(AndroidExecutor)


        }
    }

    def 'test ant executor tasks'() {
        given:
        task.releaseConf.enabled >> false

        when:
        task.singleVariant()

        then:
        with(task) {
            1 * antExecutor.executeTarget(tempDir, CLEAN)
            1 * antExecutor.executeTarget(tempDir, 'debug')
            0 * antExecutor.executeTarget(_, _)
            1 * androidExecutor.updateProject(new File('temp-variant-dir'), 'android-8', 'TestAndroidProject')
            0 * ant.copy(_)
        }
    }

    def 'test override files from variant dir'() {
        given: 'variant has its directory'
        builderInfo.variantDir >> createTempDir()
        task.releaseConf.enabled >> false

        when:
        task.singleVariant()

        then:
        1 * task.ant.copy(* _)
    }

    def 'test copy to ota'() {
        given: 'variant has its directory'
        task.releaseConf.enabled >> true

        when:
        task.singleVariant()

        then:
        1 * task.ant.copy(* _)
    }
}