package com.apphance.flow.plugins.ios.release.tasks

import com.apphance.flow.TestUtils
import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.plugins.ios.parsers.PlistParser
import spock.lang.Specification

@Mixin(TestUtils)
class UpdateVersionTaskSpec extends Specification {

    def 'updateVersion task invokes replace version on plist parser'() {
        given:
        def conf = GroovySpy(IOSConfiguration) {
            getExtVersionCode() >> '3145'
            getExtVersionString() >> '3.1.45'
        }

        and:
        def variants = [
                GroovySpy(IOSVariant) {
                    getPlist() >> GroovyMock(File)
                },
                GroovySpy(IOSVariant) {
                    getPlist() >> GroovyMock(File)
                }
        ]

        def variantsConf = GroovySpy(IOSVariantsConfiguration) {
            getVariants() >> variants
        }

        and:
        def parser = GroovyMock(PlistParser)

        and:
        def task = create(UpdateVersionTask)
        task.conf = conf
        task.variantsConf = variantsConf
        task.parser = parser

        when:
        task.updateVersion()

        then:
        1 * parser.replaceVersion(_, '3145', '3.1.45')
    }
}
