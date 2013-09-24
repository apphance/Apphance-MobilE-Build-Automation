package com.apphance.flow.plugins.ios.parsers

import org.apache.commons.collections.CollectionUtils

class XCOutputParser {

    Collection<String> readSchemes(List trimmed) {
        def startSchemes = trimmed.indexOf('Schemes:')
        if (startSchemes == -1) return []
        def schemes = trimmed[startSchemes + 1..-1]
        schemes.indexOf('') != -1 ? schemes[0..schemes.indexOf('') - 1] : schemes
    }


    Collection readIphoneSdks(List trimmed) {
        def startConfigurations = trimmed.indexOf('iOS SDKs:')
        def configurations = trimmed[startConfigurations + 1..-1]
        def onlyConfigurations = configurations[0..configurations.indexOf('') - 1]
        def output = ['iphoneos']
        onlyConfigurations.each {
            output << it[it.indexOf('-sdk ') + '-sdk '.length()..-1]
        }
        return output
    }

    Collection readIphoneSimulatorSdks(List trimmed) {
        def startConfigurations = trimmed.indexOf('iOS Simulator SDKs:')
        def configurations = trimmed[startConfigurations + 1..-1]
        def lastIndex = configurations.indexOf('')
        if (lastIndex == -1) {
            lastIndex = 0
        }
        def onlyConfigurations = configurations[0..lastIndex - 1]
        def output = ['iphonesimulator']
        onlyConfigurations.each {
            output << it[it.indexOf('-sdk ') + '-sdk '.length()..-1]
        }
        return output
    }

    Map<String, String> parseBuildSettings(List<String> trimmed) {
        if (CollectionUtils.isEmpty(trimmed))
            return [:]

        trimmed.inject([:]) { result, line ->
            def splitted = line.split('=')
            splitted.size() == 2 ? result[splitted[0].trim()] = splitted[1].trim() : null
            result
        }
    }
}
