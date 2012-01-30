package com.apphance.ameba.unit;

import static org.junit.Assert.*;

import org.junit.Test;

import com.apphance.ameba.XMLBomAwareFileReader

class XmlBomAwareFileReaderTest {
    @Test
    public void readXMLWithBom() throws Exception {
        File f = new File("testProjects/ios/testBom.plist")
        def element = new XMLBomAwareFileReader().readXMLFileIncludingBom(f)
        assertNotNull(element)
    }
}
