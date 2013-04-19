package com.apphance.ameba.configuration.android

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
        arc.projectCountry.value = country
        arc.projectLanguage.value = lang

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
}
