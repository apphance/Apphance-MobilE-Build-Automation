package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant
import com.apphance.ameba.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.plugins.ios.parsers.PlistParser
import org.gradle.api.Project
import spock.lang.Specification

import static com.apphance.ameba.configuration.ios.IOSReleaseConfiguration.getICON_PATTERN

class IOSReleaseConfigurationSpec extends Specification {

    def iosReleaseConf = new IOSReleaseConfiguration()

    def setup() {
        def iosConf = GroovySpy(IOSConfiguration)
        iosConf.project = GroovyStub(Project) {
            getRootDir() >> new File('testProjects/ios/GradleXCode/')
        }

        def variantsConf = GroovyStub(IOSVariantsConfiguration)
        variantsConf.mainVariant >> GroovyStub(AbstractIOSVariant) {
            getPlist() >> new File('testProjects/ios/GradleXCode/GradleXCode/GradleXCode-Info.plist.json')
        }

        def parser = new PlistParser()

        parser.executor = GroovyMock(IOSExecutor) {
            plistToJSON(_) >> new File('testProjects/ios/GradleXCode/GradleXCode/GradleXCode-Info.plist.json').text.split('\n')
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

    def 'test findMobileProvisionFiles'() {
        when:
        def files = iosReleaseConf.findMobileProvisionFiles()

        then:
        files.size() <= 2
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
}
