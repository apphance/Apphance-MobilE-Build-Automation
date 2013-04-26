package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.reader.PropertyReader
import spock.lang.Specification

class AndroidReleaseConfigurationSpec extends Specification {
    def arc = new AndroidReleaseConfiguration(Mock(AndroidConfiguration), null)

    def setup() {
        arc.projectURL.value = 'http://ota.polidea.pl/SATGuruAndroid-32DRDK64'
    }

    def 'base url is correct'() {
        expect:
        'http://ota.polidea.pl/'.toURL() == arc.baseURL
    }

    def 'project dir name is correct'() {
        expect:
        'SATGuruAndroid-32DRDK64' == arc.projectDirName
    }

    def 'locale is set correct'() {
        when:
        arc.country.value = country
        arc.language.value = lang

        then:
        arc.locale == locale

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
        def arc = new AndroidReleaseConfiguration(null, reader)
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
        def arc = new AndroidReleaseConfiguration(null, reader)
        arc.mailServerInternal = new StringProperty(value: mailServerProperty)

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
        arc.releaseMailFlags.validator('qrCode,imageMontage')
        arc.releaseMailFrom.validator('')
        arc.releaseMailFrom.validator(null)
        arc.releaseMailFrom.validator('Jenkins <no-reply@polidea.pl>')
        arc.releaseMailTo.validator('qwilt-team@polidea.pl')
        arc.releaseMailTo.validator('')
        arc.releaseMailTo.validator(null)
    }
}
