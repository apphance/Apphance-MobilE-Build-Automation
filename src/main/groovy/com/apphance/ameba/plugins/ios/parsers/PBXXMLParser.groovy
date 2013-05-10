package com.apphance.ameba.plugins.ios.parsers

import com.apphance.ameba.executor.IOSExecutor
import groovy.util.slurpersupport.GPathResult

import javax.inject.Inject

class PbxXmlParser {

    @Inject
    IOSExecutor executor

    String plist(String confName, String blueprintIdentifier) {
        def xml = parsedPBX()

        def targetDict = valueForKeyNode(nodeForKey(xml.dict.dict.key, blueprintIdentifier))
        def buildConfListKey = valueForKeyNode(nodeForKey(targetDict.children(), 'buildConfigurationList'))
        def buildConfListDict = valueForKeyNode(nodeForKey(xml.dict.dict.key, buildConfListKey.text()))
        def confKeys = buildConfListDict.array.string*.toString()

        def configNodes = findAll(xml.dict.dict.key, { it.text() in confKeys })

        def conf = findConfForName(configNodes, confName)

        def confDict = valueForKeyNode(conf)
        def confBuildSettings = valueForKeyNode(nodeForKey(confDict.key, 'buildSettings'))
        def plist = valueForKeyNode(nodeForKey(confBuildSettings.key, 'INFOPLIST_FILE'))

        plist
    }

    private GPathResult findConfForName(GPathResult confNodes, String confName) {
        confNodes.find {
            def confDict = valueForKeyNode(it)
            def nameValueNode = valueForKeyNode(nodeForKey(confDict.key, 'name'))
            nameValueNode.text() == confName
        }
    }

    private GPathResult nodeForKey(GPathResult xml, String key) {
        xml.find { it.text() == key }
    }

    private GPathResult valueForKeyNode(GPathResult keyNode) {
        def siblings = keyNode.parent().children()
        siblings[siblings.findIndexOf { it == keyNode } + 1] as GPathResult
    }

    private GPathResult findAll(GPathResult xml, Closure filter) {
        xml.findAll(filter)
    }

    private GPathResult parsedPBX() {
        def input = new ByteArrayInputStream(executor.pbxProjToXml().join('\n').bytes)
        new XmlSlurper().parse(input)
    }
}
