package com.apphance.ameba.plugins.ios.release.tasks

import com.apphance.ameba.plugins.ios.IOSProjectConfiguration
import com.apphance.ameba.plugins.ios.XMLBomAwareFileReader
import com.apphance.ameba.plugins.projectconfiguration.ProjectConfiguration
import com.sun.org.apache.xpath.internal.XPathAPI
import org.w3c.dom.Element

import static org.gradle.api.logging.Logging.getLogger

/**
 * Manipulation of .plist file.
 *
 */
//TODO test + refactor
class IOSPlistProcessor {

    private l = getLogger(getClass())

    private Element getParsedPlist(IOSProjectConfiguration iosConf) {
        l.debug("Reading file " + iosConf.plistFile)
        return new XMLBomAwareFileReader().readXMLFileIncludingBom(iosConf.plistFile)
    }

    public void incrementPlistVersion(IOSProjectConfiguration iosConf, ProjectConfiguration conf) {
        def root = getParsedPlist(iosConf)
        XPathAPI.selectNodeList(root,
                '/plist/dict/key[text()="CFBundleShortVersionString"]').each {
            it.nextSibling.nextSibling.textContent = conf.versionString
        }
        XPathAPI.selectNodeList(root,
                '/plist/dict/key[text()="CFBundleVersion"]').each {
            it.nextSibling.nextSibling.textContent = conf.versionCode
        }
        iosConf.plistFile.write(root as String)
    }

}
