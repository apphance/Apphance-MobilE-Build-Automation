package com.apphance.flow.plugins.android.release.tasks

import com.apphance.flow.TestUtils
import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper
import com.apphance.flow.validation.VersionValidator
import spock.lang.Specification

@Mixin(TestUtils)
class UpdateVersionTaskSpec extends Specification {

    def 'version is updated correctly'() {
        given:
        def tmpDir = temporaryDir
        def task = create(UpdateVersionTask) as UpdateVersionTask

        and:
        task.conf = GroovySpy(AndroidConfiguration) {
            getRootDir() >> tmpDir
            getVersionCode() >> '3145'
            getVersionString() >> '31.4.5'
        }
        task.variantsConf = GroovySpy(AndroidVariantsConfiguration) {
            getVariants() >> [
                    GroovyMock(AndroidVariantConfiguration) {
                        getTmpDir() >> temporaryDir
                    },
                    GroovyMock(AndroidVariantConfiguration) {
                        getTmpDir() >> temporaryDir
                    }
            ]
        }
        task.manifestHelper = GroovyMock(AndroidManifestHelper)
        task.versionValidator = new VersionValidator()

        when:
        task.updateVersion()

        then:
        1 * task.manifestHelper.updateVersion(task.variantsConf.variants[0].tmpDir, '31.4.5', '3145')
        1 * task.manifestHelper.updateVersion(task.variantsConf.variants[1].tmpDir, '31.4.5', '3145')
    }
}
