package com.apphance.ameba.unit

import com.apphance.ameba.XMLBomAwareFileReader
import org.junit.Test

import static org.junit.Assert.assertNotNull

class XmlBomAwareFileReaderTest {
    @Test
    public void readXMLWithBom() throws Exception {
        File f = new File("testProjects/ios/testBom.plist")
        def element = new XMLBomAwareFileReader().readXMLFileIncludingBom(f)
        assertNotNull(element)
    }
}
