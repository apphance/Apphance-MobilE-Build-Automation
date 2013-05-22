package com.apphance.ameba.plugins.ios.parsers

import org.apache.commons.collections.CollectionUtils

import static org.gradle.api.logging.Logging.getLogger

/**
 * Parses xcodebuild output.
 *
 */
class XCodeOutputParser {

    def l = getLogger(getClass())

    Collection readBuildableConfigurations(List trimmedOutput) {
        return readBaseConfigurations(trimmedOutput, { it != "Debug" && it != "Release" })
    }

    Collection readBuildableTargets(List trimmedOutput) {
        return readBaseTargets(trimmedOutput, { !it.endsWith('Tests') && !it.endsWith('Specs') })
    }

    Collection<String> readBaseConfigurations(List trimmed, Closure filter = { true }) {
        def startConfigurations = trimmed.indexOf('Build Configurations:')
        def configurations = trimmed[startConfigurations + 1..-1]
        def onlyConfigurations = configurations[0..configurations.indexOf('') - 1]
        return onlyConfigurations.findAll(filter) as Collection<String>
    }

    Collection<String> readBaseTargets(List trimmed, Closure filter = { true }) {
        def startTargets = trimmed.indexOf('Targets:')
        def targets = trimmed[startTargets + 1..-1]
        def onlyTargets = targets[0..targets.indexOf('') - 1]
        return onlyTargets.findAll(filter) as Collection<String>
    }

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

    String readProjectName(List trimmed) {
        String firstLine = trimmed[0]
        def matcher = firstLine =~ /.*"(.*)"/
        return matcher[0][1]
    }

    Map<String, String> parseBuildSettings(List<String> trimmed) {
        if (CollectionUtils.isEmpty(trimmed)) {
            return [:]
        }
        def result = [:]
        trimmed.each {
            def splitted = it.split('=')
            if (splitted.size() == 2) {
                result[splitted[0].trim()] = splitted[1].trim()
            }
        }
        result
    }
}
