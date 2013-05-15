package com.apphance.ameba.plugins.ios.parsers

import com.sun.org.apache.xpath.internal.XPathAPI
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.w3c.dom.Element

import javax.xml.parsers.DocumentBuilderFactory

import static org.gradle.api.logging.Logging.getLogger

/**
 * Parses plist file.
 *
 */
class MPParser {

    static Logger logger = getLogger(MPParser.class)

    static String readBundleIdFromProvisionFile(URL mobileprovisionUrl) {
        String xml = extractXML(mobileprovisionUrl)
        def builderFactory = DocumentBuilderFactory.newInstance()
        builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        builderFactory.setFeature("http://xml.org/sax/features/validation", false)
        def builder = builderFactory.newDocumentBuilder()
        InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"))
        def root = builder.parse(is).documentElement
        String bundleId
        XPathAPI.selectNodeList(root,
                '/plist/dict/dict/key[text()="application-identifier"]').each {
            bundleId = it.nextSibling.nextSibling.textContent
        }
        bundleId = bundleId.split("\\.")[1..-1].join(".")
        return bundleId
    }

    static String readBundleIdFromPlist(URL pListUrl) {
        File pListFile = new File(new URI(pListUrl.toString()))
        def root = new XMLBomAwareFileReader().readXMLFileIncludingBom(pListFile)
        String bundleId
        XPathAPI.selectNodeList(root,
                '/plist/dict/key[text()="CFBundleIdentifier"]').each {
            bundleId = it.nextSibling.nextSibling.textContent
        }
        return bundleId
    }

    static Collection<String> readUdids(URL mobileprovisionUrl) {
        String xml = extractXML(mobileprovisionUrl)
        def builderFactory = DocumentBuilderFactory.newInstance()
        builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        builderFactory.setFeature("http://xml.org/sax/features/validation", false)
        def builder = builderFactory.newDocumentBuilder()
        InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"))
        def root = builder.parse(is).documentElement
        def provisionedDevicesList = new LinkedList<String>()
        XPathAPI.selectNodeList(root,
                '/plist/dict/key[text()="ProvisionedDevices"]').each {
            def provisionedDevicesArray = it.nextSibling.nextSibling.childNodes
            provisionedDevicesArray.each {
                if (it.nodeType == org.w3c.dom.Node.ELEMENT_NODE) {
                    if (it.nodeName != "string") {
                        throw new GradleException("Expecting element name 'string' and got ${it.nodeName}")
                    }
                    provisionedDevicesList << it.firstChild.nodeValue.toString()
                }
            }
        }
        return provisionedDevicesList
    }

    static String extractXML(URL mobileprovisionUrl) {
        def lines = mobileprovisionUrl.readLines("utf-8")*.trim()
        def startPlist = lines.findIndexOf { it.startsWith('<plist') }
        def rest = lines[startPlist..-1]
        def xml = rest[0..rest.findIndexOf { it.startsWith('</plist') }]
        return xml.join("\n")
    }

    static Element getParsedPlist(File file) {

        def builderFactory = DocumentBuilderFactory.newInstance()

        builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)

        builderFactory.setFeature("http://xml.org/sax/features/validation", false)

        return new XMLBomAwareFileReader().readXMLFileIncludingBom(file)

    }
}
