package com.apphance.ameba

import javax.xml.parsers.DocumentBuilderFactory

import org.apache.tools.ant.filters.StringInputStream

/**
 * Reads BOM file (iOS) without validation.
 *
 */
public class XMLBomAwareFileReader {
    public readXMLFileIncludingBom(File pListFile) {
        return readXMLFileRootIncludingBom(pListFile).documentElement
    }

    public readXMLFileRootIncludingBom(File pListFile) {
        def builderFactory = DocumentBuilderFactory.newInstance()
        builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        builderFactory.setFeature("http://xml.org/sax/features/validation", false)
        def builder = builderFactory.newDocumentBuilder()
        CharsetToolkit toolkit = new CharsetToolkit(pListFile)
        BufferedReader reader = toolkit.getReader();
        String xml = reader.text
        StringInputStream xmlStream = new StringInputStream(xml,"utf-8")
        return builder.parse(xmlStream)
    }
}
