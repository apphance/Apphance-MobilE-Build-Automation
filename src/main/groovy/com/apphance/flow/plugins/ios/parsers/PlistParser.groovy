package com.apphance.flow.plugins.ios.parsers

import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.util.FlowUtils
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.text.SimpleTemplateEngine

import javax.inject.Inject
import java.util.regex.Pattern

import static groovy.json.JsonOutput.toJson
import static org.apache.commons.lang.StringUtils.isBlank

@Mixin(FlowUtils)
class PlistParser {

    static final PLACEHOLDER = Pattern.compile('\\$\\{([A-Z0-9a-z]+_)*([A-Z0-9a-z])+(:rfc1034identifier)?\\}')
    static final IDENTIFIERS = [
            'std': { it },
            'rfc1034identifier': { it.replaceAll('[^A-Za-z0-9-.]', '') }
    ]

    private templateEngine = new SimpleTemplateEngine()

    @Inject IOSExecutor executor

    String bundleVersion(File plist) {
        parsedJson.call(plist).CFBundleVersion
    }

    String bundleShortVersionString(File plist) {
        parsedJson.call(plist).CFBundleShortVersionString
    }

    String bundleId(File plist) {
        parsedJson.call(plist).CFBundleIdentifier
    }

    String bundleDisplayName(File plist) {
        parsedJson.call(plist).CFBundleDisplayName
    }

    List<String> iconFiles(File plist) {
        def json = parsedJson.call(plist)
        (json.CFBundleIconFiles + json.CFBundleIcons.CFBundlePrimaryIcon.CFBundleIconFiles)?.unique()?.sort()
    }

    private Closure<Map> parsedJson = { File plist ->
        def text = executor.plistToJSON(plist).join('\n')
        new JsonSlurper().parseText(text) as Map
    }.memoize()

    void replaceVersion(File plist, String versionCode, String versionString) {
        def json = parsedJson.call(plist)
        json.CFBundleVersion = versionCode
        json.CFBundleShortVersionString = versionString

        def tmpFile = getTempFile('json.plist') << toJson(json)

        plist.text = executor.plistToXML(tmpFile).join('\n')
    }

    String evaluate(String value, String target, String configuration) {

        if (isBlank(value))
            return value

        def matcher = PLACEHOLDER.matcher(value)
        def binding = [:]

        while (matcher.find()) {
            def placeholder = matcher.group()
            def formatter = IDENTIFIERS.std
            if (placeholder.contains(':')) {
                String formatterId = placeholder.substring(placeholder.indexOf(':') + 1, placeholder.indexOf('}'))
                formatter = IDENTIFIERS[formatterId]
                value = value.replaceFirst(":$formatterId", '')
                placeholder = placeholder.replace(":$formatterId", '')
            }
            def unfoldedPlaceholder = unfoldPlaceholder(placeholder)
            binding[unfoldedPlaceholder] = formatter(executor.buildSettings(target, configuration)[unfoldedPlaceholder])
        }
        templateEngine.createTemplate(value).make(binding)
    }

    static String unfoldPlaceholder(String value) {
        isBlank(value) ? '' : value.replaceAll('[}{$]', '')
    }
}
