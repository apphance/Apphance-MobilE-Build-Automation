package com.apphance.flow.plugins.android.builder

import com.apphance.flow.TestUtils
import com.apphance.flow.configuration.android.AndroidBuildMode
import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.properties.BooleanProperty
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.AndroidExecutor
import com.apphance.flow.executor.AntExecutor
import com.apphance.flow.executor.command.CommandFailedException
import com.apphance.flow.plugins.android.buildplugin.tasks.AndroidProjectUpdater
import com.apphance.flow.plugins.android.buildplugin.tasks.SingleVariantTask
import com.apphance.flow.plugins.release.FlowArtifact
import com.apphance.flow.util.FlowUtils
import org.gradle.api.AntBuilder as AntBuilder
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import spock.lang.Specification

import static com.apphance.flow.executor.AntExecutor.CLEAN
import static com.google.common.io.Files.createTempDir

@Mixin([TestUtils, FlowUtils])
class SingleVariantTaskUnitSpec extends Specification {

    def task = create SingleVariantTask
    def variantDir = temporaryDir
    AndroidBuilderInfo builderInfo

    def setup() {
        new File(variantDir, 'project.properties').createNewFile()
        builderInfo = GroovyStub(AndroidBuilderInfo) {
            getTmpDir() >> variantDir
            getMode() >> AndroidBuildMode.DEBUG
            getOriginalFile() >> getTempFile()
        }
        task.with {
            artifactProvider = GroovyStub(AndroidArtifactProvider) {
                builderInfo(_) >> builderInfo
                artifact(_) >> GroovyStub(FlowArtifact) {
                    getLocation() >> getTempFile()
                }
            }

            projectUpdater = GroovySpy(AndroidProjectUpdater)
            conf = GroovyStub(AndroidConfiguration) {
                getTarget() >> new StringProperty(value: 'android-8')
                getProjectName() >> new StringProperty(value: 'TestAndroidProject')
            }
            projectUpdater.executor = GroovyMock(AndroidExecutor)
            releaseConf = GroovyStub(AndroidReleaseConfiguration)
            variant = GroovyStub(AndroidVariantConfiguration) {
                getTmpDir() >> variantDir
                getOldPackage() >> new StringProperty()
                getNewPackage() >> new StringProperty()
                getMergeManifest() >> new BooleanProperty(value: 'true')
                getVariantDir() >> new FileProperty(value: temporaryDir)
            }

            ant = GroovyMock(AntBuilder)
            antExecutor = GroovyMock(AntExecutor)
        }
    }

    def 'test ant executor tasks'() {
        given:
        task.releaseConf.enabled >> false

        when:
        task.singleVariant()

        then:
        with(task) {
            1 * projectUpdater.updateRecursively(variantDir, 'android-8', 'TestAndroidProject')
            1 * projectUpdater.executor.updateProject(variantDir, 'android-8', 'TestAndroidProject')
            1 * antExecutor.executeTarget(variantDir, CLEAN)
            1 * antExecutor.executeTarget(variantDir, 'debug')
            1 * ant.copy(* _)

            0 * antExecutor.executeTarget(_, _)
        }
    }

    def 'test override files from variant dir'() {
        given: 'variant has its directory'
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
        2 * task.ant.copy(* _)
    }

    def 'test manifest merge'() {
        given:
        def main = tempFile << new File('src/test/resources/com/apphance/flow/android/AndroidManifestToMergeMain.xml').text
        def variantA = new File('src/test/resources/com/apphance/flow/android/AndroidManifestToMergeVariantA.xml')
        def variantB = new File('src/test/resources/com/apphance/flow/android/AndroidManifestToMergeVariantB.xml')

        expect:
        main.exists() && variantA.exists() && variantB.exists()
        permissions(main) == []
        permissions(variantA) == ['android.permission.INTERNET']
        permissions(variantB) == ['android.permission.READ_CALENDAR']

        when:
        task.mergeManifest(main, main, variantA, variantB)

        then:
        permissions(main) == ['android.permission.INTERNET', 'android.permission.READ_CALENDAR']
    }

    def 'test manifest merge throws exception'() {
        given:
        def main = tempFile << new File('src/test/resources/com/apphance/flow/android/AndroidManifestToMergeMain.xml').text
        def incorrectManifest = tempFile << new File('src/test/resources/com/apphance/flow/android/AndroidManifestToMergeVariantA.xml').
                text.replace('android:minSdkVersion="7"', 'android:minSdkVersion="1000"')

        expect:
        main.exists() && incorrectManifest.exists()
        permissions(main) == []
        permissions(incorrectManifest) == ['android.permission.INTERNET']

        when:
        task.mergeManifest(main, main, incorrectManifest)

        then:
        GradleException ex = thrown()
        ex.message == 'Error during merging manifests.'
    }

    def 'test incorrect android manifest hint'() {
        given:
        task.logger = GroovyMock(Logger)
        task.antExecutor = GroovyStub(AntExecutor) {
            executeTarget(_, _) >> {
                throw new CommandFailedException(null, null, tempFile << content)
            }
        }

        when:
        task.executeBuildTarget(null, null)

        then:
        thrown(CommandFailedException)
        number * task.logger.error("Error during source compilation. Probably some non-activity class was configured as activity in AndroidManifest.xml.\n" +
                "Make sure that all <activity> tags in your manifest points to some activity classes and not to other classes like Fragment.")

        where:
        number | content
        0      | "simple output"
        1      | 'method onStart in class Apphance cannot be applied to given types'
    }

    List<String> permissions(File manifest) {
        new XmlSlurper().parse(manifest).'uses-permission'.@'android:name'*.text()
    }
}
