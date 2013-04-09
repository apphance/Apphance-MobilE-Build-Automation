package com.apphance.ameba.configuration.android

import com.apphance.ameba.util.SortedProperties
import org.apache.commons.io.FileUtils
import spock.lang.Specification

import static com.google.common.io.Files.newWriter
import static java.nio.charset.StandardCharsets.UTF_8

class SortedPropertiesSpec extends Specification {

    def 'properties are sorted according to key'() {
        given:
        Properties props = new SortedProperties()
        def propFile = File.createTempFile('prefix', 'sufix')
        propFile.deleteOnExit()

        when:
        props.put('android.test', '123')
        props.put('ios.test', '123')
        props.put('android.release', '123')
        props.put('ios.analysis', '123')
        props.put('ios.release', '123')
        props.put('xyz.release', '123')
        props.put('def.release', '123')
        props.put('abc', '123')

        props.store(newWriter(propFile, UTF_8), '')

        def lines = FileUtils.readLines(propFile)
        lines.removeAll { String it -> it.startsWith('#') }
        def sortedLines = ([] + lines).sort()

        then:
        lines == sortedLines
    }
}
