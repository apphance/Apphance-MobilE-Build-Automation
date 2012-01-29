package com.apphance.ameba.unit.ios;

import static org.junit.Assert.*
import groovy.xml.DOMBuilder

import javax.xml.parsers.DocumentBuilderFactory

import org.hamcrest.core.IsEqual
import org.junit.Before
import org.junit.Test

import com.apphance.ameba.ios.MPParser

class MPParserTest {

    URL provisioningFile
    URL plistFile
    @Before
    void setUp(){
        provisioningFile = this.getClass().getResource("Test.mobileprovision")
        plistFile = this.getClass().getResource("Test.plist")
    }

    @Test
    public void testXMLExtraction() throws Exception {
        String xml = MPParser.extractXML(provisioningFile)
        def root  = new DOMBuilder(DocumentBuilderFactory.newInstance().newDocumentBuilder()).parseText(xml)
        assertNotNull(root)
    }

    @Test
    public void testBundleIdReading() throws Exception {
        String bundleId = MPParser.readBundleIdFromProvisionFile(provisioningFile)
        assertThat(bundleId, new IsEqual("com.apphance.Some"))
    }

    @Test
    public void testBundleIdReadingFromPlistFile() throws Exception {
        String bundleId = MPParser.readBundleIdFromPlist(plistFile)
        assertThat(bundleId, new IsEqual("com.apphance.Some"))
    }

    @Test
    public void testUdidReading() throws Exception {
        def udids = MPParser.readUdids(provisioningFile)
        assertThat(udids, new IsEqual([
            "2b4af846e1519701a8cf0ef3602e7178ce5c6266",
            "3a47a18141e37f24319d686a672c66eafa242919",
            "defbf303c7a284a2746f39a3a30eb9baf9c37949",
            "18250ed94639fb94517f9cd55c039308a9b5d2ad",
            "8707787157d3a4bacb955afeacd720f6e513a1f7",
            "c57f80d840c2a35c048a68a6d9876c057d20b67f",
            "68291e1c9d027df4ad980f0310ac0c6ca88e27d9",
            "e202fda7d6ab419d847d9fbf6d834ac7f2b7964f",
            "6b2f6abb29f5038033ae59acfebb48572daa4e40",
            "dfd98a32fd20a6dabbd71a3d52ba47ba25ff6d35",
            "4b75d1db0b8ce04fde8a7ccd0d576f11f4c72415",
            "3652f997aeae003dfd067376ea1852a97fc495c6",
            "ce8c6f2d2c25fe9882169fa00ff008bfbe7b5679",
            "cba04d41c62bcb845193a6942b13d7876e7849e0",
            "139fbb263b2d9d78417b90c71dbee6aa97f04a0f",
            "a4c7a80d514b82862bf0ffb5e3351adb921a36ec",
            "4ab786cc4f6adb7ce043f1f81f818cf8141cacf7",
            "62607506a5bfc4c1e3ef070f47161ed024d3111f",
            "a0dbf1910911127a018e04036f7609bea754a97c",
            "2b4af846e1519701a8cf0ef3602e7178ce5ce5c6",
            "d2ade15edd9fb52a78e364057f6ddc8f05fd040b",
            "9d3b8b84091edc477355a94886add545160845ea",
            "7a36afb2bbefb2a332715786e1ce9d02fa899188",
            "719ad4b5ce7d3cab8993e255d48327d04e79ff39",
            "bea8af0efd1674d7efe2a82ec9e66aab74eee6ea",
            "074b4502dcf5af019a5b1f7e3eb304129314d5d7",
            "8177bb61991f38d383a4431ea5cc9e371de4c229",
            "06811917de5ffc52d09399b59e4c22383bc7db65",
            "bae6b26dc9daf991ea3687b0977b59825dc9b125",
            "aa3c81be7d220305d5833b423e07e2f10e9a93dd",
            "00dd7ec340caec141a1da657f25ad6bc1d5ab84d",
            "b40cf2b88ff403a7d40378fc34f7b982863fab41",
            "6baca380803e1498470b309863d0990bfc6c5fd5",
            "d236d087781bf2d33426e227ceb1bbf84d60f862",
            "57a2821a2ff87efc075a15e48182ee8321494780"
        ]))
    }
}
