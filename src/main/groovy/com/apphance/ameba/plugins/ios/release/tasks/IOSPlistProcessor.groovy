package com.apphance.ameba.plugins.ios.release.tasks

import com.apphance.ameba.plugins.ios.parsers.XMLBomAwareFileReader
import com.sun.org.apache.xpath.internal.XPathAPI
import org.w3c.dom.Element

/**
 * Manipulation of .plist file.
 *
 */
//TODO test + refactor
class IOSPlistProcessor {

    void incrementPlistVersion(File plist, String versionCode, String versionString) {
        def root = getParsedPlist(plist)
        XPathAPI.selectNodeList(root,
                '/plist/dict/key[text()="CFBundleShortVersionString"]').each {
            it.nextSibling.nextSibling.textContent = versionString
        }
        XPathAPI.selectNodeList(root,
                '/plist/dict/key[text()="CFBundleVersion"]').each {
            it.nextSibling.nextSibling.textContent = versionCode
        }
        plist.delete()
        plist.write(root as String)
    }

    private Element getParsedPlist(File plist) {
        return new XMLBomAwareFileReader().readXMLFileIncludingBom(plist)
    }

}
