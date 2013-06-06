package com.apphance.ameba.plugins.ios.parsers

import com.apphance.ameba.executor.IOSExecutor
import groovy.json.JsonSlurper
import groovy.text.SimpleTemplateEngine
import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil

import javax.inject.Inject
import java.util.regex.Pattern

import static org.apache.commons.lang.StringUtils.isBlank
import static org.apache.commons.lang.StringUtils.isNotBlank
import static org.gradle.api.logging.Logging.getLogger

class PlistParser {

    private l = getLogger(getClass())

    static final PLACEHOLDER = Pattern.compile('\\$\\{([A-Z0-9a-z]+_)*([A-Z0-9a-z])+(:rfc1034identifier)?\\}')
    static final IDENTIFIERS = [
            'std': { it },
            'rfc1034identifier': { it.replaceAll('[^A-Za-z0-9-.]', '') }
    ]

    private templateEngine = new SimpleTemplateEngine()

    @Inject IOSExecutor executor

    String versionCode(File plist) {
        def json = parsedJson(plist)
        json.CFBundleVersion
    }

    String versionString(File plist) {
        def json = parsedJson(plist)
        json.CFBundleShortVersionString
    }

    String bundleId(File plist) {
        def json = parsedJson(plist)
        json.CFBundleIdentifier
    }

    String bundleDisplayName(File plist) {
        def json = parsedJson(plist)
        json.CFBundleDisplayName
    }

    List<String> getIconFiles(File plist) {
        def json = parsedJson(plist)
        (json.CFBundleIconFiles + json.CFBundleIcons.CFBundlePrimaryIcon.CFBundleIconFiles)?.unique()?.sort()
    }

    private Object parsedJson(File plist) {
        def text = executor.plistToJSON(plist).join('\n')
        new JsonSlurper().parseText(text)
    }

    void replaceBundledId(File plist, String oldBundleId, String newBundleId) {
        l.info("Attempting to replace oldBundleId ($oldBundleId) with newBundleId ($newBundleId) in file: ${plist.absolutePath}")
        def xml = new XmlSlurper().parse(plist)
        def keyNode = xml.dict.key.find { it.text() == 'CFBundleIdentifier' }
        def valueNode = nextNode(keyNode)
        def value = valueNode.text()
        if (newBundleId.startsWith(oldBundleId)) {
            String newResult = newBundleId + value.substring(oldBundleId.length())
            valueNode.replaceBody(newResult)
        } else {
            l.info("Bundle ID will not be replaced: newBundleId ($newBundleId) does not start with oldBundleId ($oldBundleId)")
        }
        plist.text = XmlUtil.serialize(xml)
    }

    void replaceVersion(File plist, String versionCode, String versionString) {
        def xml = new XmlSlurper().parse(plist)

        def versionCodeKey = xml.dict.key.find { it.text() == 'CFBundleVersion' }
        def versionCodeValueNode = nextNode(versionCodeKey)
        versionCodeValueNode.replaceBody(versionCode)

        def versionStringKey = xml.dict.key.find { it.text() == 'CFBundleShortVersionString' }
        def versionStringValueNode = nextNode(versionStringKey)
        versionStringValueNode.replaceBody(versionString)

        plist.text = XmlUtil.serialize(xml)
    }

    private GPathResult nextNode(GPathResult node) {
        def siblings = node.parent().children()
        siblings[siblings.findIndexOf { it == node } + 1]
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

    static boolean isPlaceholder(String value) {
        isNotBlank(value) && value.matches('\\$\\{([A-Z]+_)*([A-Z])+\\}')
    }

    static boolean isNotPlaceHolder(String value) {
        !isPlaceholder(value)
    }
}
