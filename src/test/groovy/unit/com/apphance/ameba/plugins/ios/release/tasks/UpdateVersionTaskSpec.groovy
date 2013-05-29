package com.apphance.ameba.plugins.ios.release.tasks

import com.apphance.ameba.TestUtils
import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant
import com.apphance.ameba.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.ameba.plugins.ios.parsers.PlistParser
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
                GroovySpy(AbstractIOSVariant) {
                    getPlist() >> GroovyMock(File)
                },
                GroovySpy(AbstractIOSVariant) {
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
        2 * parser.replaceVersion(_, '3145', '3.1.45')
    }
}
