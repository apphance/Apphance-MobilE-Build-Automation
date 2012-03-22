package com.apphance.ameba.ios.plugins.release

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.XMLBomAwareFileReader
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.sun.org.apache.xpath.internal.XPathAPI

class IOSPlistProcessor {

    static Logger logger = Logging.getLogger(IOSReleasePlugin.class)

    public org.w3c.dom.Element getParsedPlist(Project project, IOSProjectConfiguration iosConf) {
        logger.debug("Reading file " + iosConf.plistFile)
        return new XMLBomAwareFileReader().readXMLFileIncludingBom(iosConf.plistFile)
    }

    public void incrementPlistVersion(Project project, IOSProjectConfiguration iosConf, ProjectConfiguration conf) {
        def root = getParsedPlist(project, iosConf)
        XPathAPI.selectNodeList(root,
                '/plist/dict/key[text()="CFBundleShortVersionString"]').each{
                    it.nextSibling.nextSibling.textContent = conf.versionString
                }
        conf.versionCode += 1
        XPathAPI.selectNodeList(root,
                '/plist/dict/key[text()="CFBundleVersion"]').each{
                    it.nextSibling.nextSibling.textContent = conf.versionCode
                }
        iosConf.plistFile.write(root as String)
    }

}
