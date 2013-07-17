package com.apphance.flow.configuration.release

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.configuration.reader.PropertyReader
import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper
import org.gradle.api.GradleException
import org.gradle.api.Project
import spock.lang.Specification

import static com.apphance.flow.configuration.release.ReleaseConfiguration.validateMailPort
import static com.apphance.flow.configuration.release.ReleaseConfiguration.validateMailServer

class ReleaseConfigurationSpec extends Specification {

    def releaseConf = GroovySpy(ReleaseConfiguration)

    def setup() {
        releaseConf.releaseUrl.value = 'http://ota.polidea.pl/SATGuruAndroid-32DRDK64'
    }

    def 'project dir name is correct'() {
        expect:
        'SATGuruAndroid-32DRDK64' == releaseConf.releaseDirName
    }

    def 'locale is set correct'() {
        when:
        releaseConf.country.value = country
        releaseConf.language.value = lang

        then:
        releaseConf.locale == locale

        where:
        lang | country | locale
        null | null    | Locale.getDefault()
        null | ''      | Locale.getDefault()
        ''   | null    | Locale.getDefault()
        '\n' | ''      | Locale.getDefault()
        null | ''      | Locale.getDefault()
        ''   | 'US'    | Locale.getDefault()
        'en' | 'US'    | new Locale('en', 'US')
        'pl' | ''      | new Locale('pl')
        'pl' | null    | new Locale('pl')
    }

    def 'mail port is read from correct source'() {
        given:
        def reader = Mock(PropertyReader)
        reader.systemProperty('mail.port') >> mailPortSystem
        reader.envVariable('MAIL_PORT') >> mailPortEnv

        and:
        def arc = new AndroidReleaseConfiguration()
        arc.reader = reader
        arc.mailPortInternal = new StringProperty(value: mailPortProperty)

        expect:
        arc.mailPort == mailPortExpected

        where:
        mailPortSystem | mailPortEnv | mailPortProperty | mailPortExpected
        null           | null        | null             | ''
        '25'           | null        | null             | '25'
        '25'           | '26'        | '27'             | '25'
        '25'           | null        | '27'             | '25'
        '25'           | '26'        | null             | '25'
        null           | '26'        | null             | '26'
        null           | '26'        | '27'             | '26'
        null           | null        | '27'             | '27'
    }

    def 'mail server is read from correct source'() {
        given:
        def reader = Mock(PropertyReader)
        reader.systemProperty('mail.server') >> mailServerSystem
        reader.envVariable('MAIL_SERVER') >> mailServerEnv

        and:
        def arc = new AndroidReleaseConfiguration()
        arc.reader = reader
        arc.mailServerInternal.value = mailServerProperty

        expect:
        arc.mailServer == mailPortExpected

        where:
        mailServerSystem | mailServerEnv | mailServerProperty | mailPortExpected
        null             | null          | null               | ''
        'imap.system.pl' | null          | null               | 'imap.system.pl'
        'imap.system.pl' | 'imap.env.pl' | 'imap.prop.pl'     | 'imap.system.pl'
        'imap.system.pl' | null          | 'imap.prop.pl'     | 'imap.system.pl'
        'imap.system.pl' | 'imap.env.pl' | null               | 'imap.system.pl'
        null             | 'imap.env.pl' | null               | 'imap.env.pl'
        null             | 'imap.env.pl' | 'imap.prop.pl'     | 'imap.env.pl'
        null             | null          | 'imap.prop.pl'     | 'imap.prop.pl'
    }

    def 'mail validators'() {
        expect:
        releaseConf.releaseMailFlags.validator('qrCode,imageMontage')
        releaseConf.releaseMailFrom.validator('')
        releaseConf.releaseMailFrom.validator(null)
        releaseConf.releaseMailFrom.validator('Jenkins <no-reply@polidea.pl>')
        releaseConf.releaseMailTo.validator('qwilt-team@polidea.pl')
        releaseConf.releaseMailTo.validator('')
        releaseConf.releaseMailTo.validator(null)
    }

    def 'possible icons are found'() {
        given:
        def projectDir = 'testProjects/android/android-basic'

        and:
        def ac = new AndroidConfiguration()
        ac.project = GroovyStub(Project) {
            getRootDir() >> new File(projectDir)
            file('res') >> new File(projectDir, 'res')
        }

        and:
        def arc = new AndroidReleaseConfiguration(androidConf: ac, manifestHelper: new AndroidManifestHelper())

        expect:
        arc.possibleIcons().sort() == ['res/drawable-hdpi/icon.png', 'res/drawable/icon.png']
    }

    def 'default icons are found'() {
        given:
        def projectDir = 'testProjects/android/android-basic'

        and:
        def ac = new AndroidConfiguration()
        ac.project = GroovyStub(Project) {
            getRootDir() >> new File(projectDir)
            file('res') >> new File(projectDir, 'res')
        }

        and:
        def arc = new AndroidReleaseConfiguration(androidConf: ac, manifestHelper: new AndroidManifestHelper())

        expect:
        arc.defaultIcon().absolutePath.endsWith('drawable/icon.png')
    }

    def 'fields from superclass are also accessible'() {
        given:
        def fields = new AndroidReleaseConfiguration().propertyFields

        expect:
        fields.size() > 0

        and:
        fields*.name.containsAll('release.icon', 'release.url')
    }


    def 'language validator'() {
        expect:
        releaseConf.language.validator(input) == result

        where:
        input | result
        'en'  | true
        'pl'  | true
        'pln' | false
        'p'   | false
        ''    | false
        'EN'  | false
        'eN'  | false
        'En'  | false
    }

    def 'country validator'() {
        expect:
        releaseConf.country.validator(input) == result

        where:
        input | result
        'us'  | false
        'Us'  | false
        'uS'  | false
        'US'  | true
        'PL'  | true
        'USA' | false
        'pln' | false
    }

    def 'mail port is validated correctly when empty'() {
        when:
        validateMailPort(mailPort)

        then:
        def e = thrown(GradleException)
        e.message =~ 'Property \'mail.port\' has invalid value!'

        where:
        mailPort << [null, '', '  \t', 'with letter', 'withletter', '123-123']
    }

    def 'mail port is validated correctly when set'() {
        when:
        validateMailPort(mailPort)

        then:
        noExceptionThrown()

        where:
        mailPort << ['121', '1']
    }

    def 'mail server is validated correctly when empty'() {
        when:
        validateMailServer(mailServer)

        then:
        def e = thrown(GradleException)
        e.message =~ 'Property \'mail.server\' has invalid value!'

        where:
        mailServer << [null, '  ', '  \t', 'with\tletter', 'with space']
    }

    def 'mail server is validated correctly when set'() {
        when:
        validateMailServer(mailServer)

        then:
        noExceptionThrown()

        where:
        mailServer << ['releaseString', 'release_String', 'relase_String_123_4']
    }
}
