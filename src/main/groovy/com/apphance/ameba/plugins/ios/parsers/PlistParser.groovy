package com.apphance.ameba.plugins.ios.parsers

import com.apphance.ameba.executor.IOSExecutor
import groovy.json.JsonSlurper
import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil

import javax.inject.Inject

class PlistParser {

    @Inject
    IOSExecutor executor

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

    private Object parsedJson(File plist) {
        def text = executor.plistToJSON(plist).join('\n')
        new JsonSlurper().parseText(text)
    }

    String replaceBundledId(File plist, String oldBundleId, String newBundleId) {
        def xml = new XmlSlurper().parse(plist)
        def keyNode = xml.dict.key.find { it.text() == 'CFBundleIdentifier' }
        def valueNode = nextNode(keyNode)
        def value = valueNode.text()
        if (newBundleId.startsWith(oldBundleId)) {
            String newResult = newBundleId + value.substring(oldBundleId.length())
            valueNode.replaceBody(newResult)
        }
        XmlUtil.serialize(xml)
    }

    String replaceVersion(File plist, String versionCode, String versionString) {
        def xml = new XmlSlurper().parse(plist)

        def versionCodeKey = xml.dict.key.find { it.text() == 'CFBundleVersion' }
        def versionCodeValueNode = nextNode(versionCodeKey)
        versionCodeValueNode.replaceBody(versionCode)

        def versionStringKey = xml.dict.key.find { it.text() == 'CFBundleShortVersionString' }
        def versionStringValueNode = nextNode(versionStringKey)
        versionStringValueNode.replaceBody(versionString)

        XmlUtil.serialize(xml)
    }

    private GPathResult nextNode(GPathResult node) {
        def siblings = node.parent().children()
        siblings[siblings.findIndexOf { it == node } + 1]
    }
}
