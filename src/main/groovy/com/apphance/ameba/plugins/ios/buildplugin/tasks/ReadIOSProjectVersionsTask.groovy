package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.plugins.ios.IOSProjectConfiguration
import com.apphance.ameba.plugins.ios.MPParser
import com.apphance.ameba.plugins.projectconfiguration.ProjectConfiguration
import com.sun.org.apache.xpath.internal.XPathAPI
import org.gradle.api.Project

import static com.apphance.ameba.PropertyCategory.getProjectConfiguration
import static com.apphance.ameba.PropertyCategory.isPropertyOrEnvironmentVariableDefined
import static com.apphance.ameba.plugins.ios.buildplugin.IOSConfigurationRetriever.getIosProjectConfiguration
import static org.gradle.api.logging.Logging.getLogger

class ReadIOSProjectVersionsTask {

    private l = getLogger(getClass())

    private Project project
    private ProjectConfiguration conf
    private IOSProjectConfiguration iosConf

    ReadIOSProjectVersionsTask(Project project) {
        this.project = project
        this.conf = getProjectConfiguration(project)
        this.iosConf = getIosProjectConfiguration(project)
    }

    void readIOSProjectVersions() {
        def root = MPParser.getParsedPlist(iosConf.pListFileName, project)
        if (root != null) {
            XPathAPI.selectNodeList(root,
                    '/plist/dict/key[text()="CFBundleShortVersionString"]').each {
                conf.versionString = it.nextSibling.nextSibling.textContent
            }
            XPathAPI.selectNodeList(root,
                    '/plist/dict/key[text()="CFBundleVersion"]').each {
                def versionCodeString = it.nextSibling.nextSibling.textContent
                try {
                    conf.versionCode = versionCodeString.toLong()
                } catch (NumberFormatException e) {
                    l.lifecycle("Format of the ${versionCodeString} is not numeric. Starting from 1.")
                    conf.versionCode = 0
                }
            }
            if (!isPropertyOrEnvironmentVariableDefined(project, 'version.string')) {
                l.lifecycle("Version string is updated to SNAPSHOT because it is not release build")
                conf.versionString = conf.versionString + "-SNAPSHOT"
            } else {
                l.lifecycle("Version string is not updated to SNAPSHOT because it is release build")
            }
        }
    }
}
