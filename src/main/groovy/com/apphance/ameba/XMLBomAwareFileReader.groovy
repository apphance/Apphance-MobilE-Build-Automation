package com.apphance.ameba

import javax.xml.parsers.DocumentBuilderFactory

import org.apache.tools.ant.filters.StringInputStream

public class XMLBomAwareFileReader {
    public readXMLFileIncludingBom(File pListFile) {
        def builder     = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        CharsetToolkit toolkit = new CharsetToolkit(pListFile)
        BufferedReader reader = toolkit.getReader();
        String xml = reader.text
        StringInputStream xmlStream = new StringInputStream(xml,"utf-8")
        return builder.parse(xmlStream).documentElement
    }
}
