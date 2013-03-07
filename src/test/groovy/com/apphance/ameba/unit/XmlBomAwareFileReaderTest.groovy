package com.apphance.ameba.unit

import com.apphance.ameba.XMLBomAwareFileReader
import org.junit.Test

import static org.junit.Assert.assertNotNull

class XmlBomAwareFileReaderTest {
    @Test
    public void readXMLWithBom() throws Exception {
        def fileUrl = getClass().getResource('testBom.plist')
        def file = new File(fileUrl.file)
        def element = new XMLBomAwareFileReader().readXMLFileIncludingBom(file)
        assertNotNull(element)
    }
}
