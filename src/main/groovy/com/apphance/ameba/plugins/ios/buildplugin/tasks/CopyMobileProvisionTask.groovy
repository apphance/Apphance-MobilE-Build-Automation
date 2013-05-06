package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.plugins.ios.IOSProjectConfiguration
import org.gradle.api.Project

import static com.apphance.ameba.plugins.ios.buildplugin.IOSConfigurationRetriever.getIosProjectConfiguration

class CopyMobileProvisionTask {

    private AntBuilder ant
    private IOSProjectConfiguration iosConf

    CopyMobileProvisionTask(Project project) {
        this.ant = project.ant
        this.iosConf = getIosProjectConfiguration(project)
    }

    void copyMobileProvision() {
        def userHome = System.getProperty("user.home")
        def mobileProvisionDirectory = userHome + "/Library/MobileDevice/Provisioning Profiles/"
        new File(mobileProvisionDirectory).mkdirs()
        ant.copy(todir: mobileProvisionDirectory, overwrite: true) {
            fileset(dir: iosConf.distributionDirectory) { include(name: "*.mobileprovision") }
        }
    }
}
