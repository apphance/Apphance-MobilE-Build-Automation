package com.apphance.flow.configuration.ios

import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.parsers.PlistParser
import com.apphance.flow.plugins.ios.parsers.PlistParserSpec
import com.google.common.io.Files
import org.gradle.api.Project
import spock.lang.Specification

import static com.apphance.flow.configuration.ios.IOSReleaseConfiguration.getICON_PATTERN

class IOSReleaseConfigurationSpec extends Specification {

    def iosReleaseConf = new IOSReleaseConfiguration()

    def setup() {
        def iosConf = GroovySpy(IOSConfiguration)
        iosConf.project = GroovyStub(Project) {
            getRootDir() >> new File('testProjects/ios/GradleXCode/')
        }

        def variantsConf = GroovyStub(IOSVariantsConfiguration)
        variantsConf.mainVariant >> GroovyStub(IOSVariant) {
            getPlist() >> new File(PlistParserSpec.class.getResource('Test.plist.json').toURI())
        }

        def parser = new PlistParser()

        parser.executor = GroovyMock(IOSExecutor) {
            plistToJSON(_) >> new File(PlistParserSpec.class.getResource('Test.plist.json').toURI()).text.split('\n')
        }

        iosReleaseConf.conf = iosConf
        iosReleaseConf.iosVariantsConf = variantsConf
        iosReleaseConf.plistParser = parser
    }

    def 'test defaultIcon'() {
        expect:
        iosReleaseConf.defaultIcon().path == 'icon.png'
    }

    def 'test possibleIcons'() {
        expect:
        iosReleaseConf.possibleIcons().containsAll(['icon.png', 'icon_retina.png'])
    }

    def 'mobile provision file are found'() {
        when:
        def files = iosReleaseConf.findMobileProvisionFiles()

        then:
        files.size() == 1
        'Ameba_Test_Project.mobileprovision' in files*.name

    }

    def 'test canBeEnabled'() {
        expect:
        iosReleaseConf.canBeEnabled()
    }

    def 'test matching icon pattern'() {
        expect:
        ok ==~ ICON_PATTERN

        where:
        ok << ['Icon.png', 'icon.png', 'Icon@2x.png', 'Icon-72.png', 'icon-small.png', 'abcIcOnaaa.png']
    }

    def 'test not matching icon pattern'() {
        expect:
        !(notMatching ==~ ICON_PATTERN)

        where:
        notMatching << ['con.png', 'icoan.png', 'icon.jpg', 'icon', 'ico.png']
    }

    def 'non existing icon handled'() {
        given:
        def rootDir = Files.createTempDir()

        and:
        def releaseConf = new IOSReleaseConfiguration(conf: GroovyStub(IOSConfiguration) {
            getRootDir() >> rootDir
        })

        when:
        releaseConf.defaultIcon()
        def value = releaseConf.iconFile.defaultValue()

        then:
        noExceptionThrown()
        value == null

        cleanup:
        rootDir.deleteDir()
    }
}
