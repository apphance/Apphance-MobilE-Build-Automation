package com.apphance.flow.plugins.ios.parsers

import com.apphance.flow.executor.IOSExecutor
import groovy.util.slurpersupport.GPathResult

import javax.inject.Inject

class MobileProvisionParser {

    @Inject IOSExecutor executor

    String bundleId(File mobileprovision) {
        def xml = parsedXml(mobileprovisionToXml(mobileprovision))
        def keyNode = xml.dict.dict.key.find { it.text() == 'application-identifier' }
        def valueNode = nextNode(keyNode)
        def bundleId = valueNode.text()
        bundleId.split('\\.')[1..-1].join('.')
    }

    Collection<String> udids(File mobileprovision) {
        def xml = parsedXml(mobileprovisionToXml(mobileprovision))
        def keyNode = xml.dict.key.find { it.text() == 'ProvisionedDevices' }
        def valueNode = nextNode(keyNode)
        valueNode.children()*.text()
    }

    private GPathResult nextNode(GPathResult node) {
        def siblings = node.parent().children()
        siblings[siblings.findIndexOf { it == node } + 1]
    }

    private GPathResult parsedXml(String text) {
        new XmlSlurper().parseText(text)
    }

    private String mobileprovisionToXml(File mobileprovision) {
        executor.mobileprovisionToXml(mobileprovision).join('\n')
    }
}
