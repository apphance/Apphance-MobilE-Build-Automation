package com.apphance.ameba.plugins.ios.release

import com.apphance.ameba.plugins.projectconfiguration.ProjectConfiguration
import com.apphance.ameba.plugins.ios.XMLBomAwareFileReader
import com.apphance.ameba.plugins.ios.IOSProjectConfiguration
import com.sun.org.apache.xpath.internal.XPathAPI
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.w3c.dom.Element

/**
 * Manipulation of .plist file.
 *
 */
class IOSPlistProcessor {

    static Logger logger = Logging.getLogger(IOSReleasePlugin.class)

    private Element getParsedPlist(IOSProjectConfiguration iosConf) {
        logger.debug("Reading file " + iosConf.plistFile)
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
