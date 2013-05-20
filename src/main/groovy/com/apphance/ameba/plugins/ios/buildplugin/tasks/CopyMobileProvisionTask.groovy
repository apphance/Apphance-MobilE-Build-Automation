package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.configuration.ios.variants.IOSVariantsConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD

class CopyMobileProvisionTask extends DefaultTask {

    static final NAME = 'copyMobileProvision'
    String description = 'Copies mobile provision file to the user library'
    String group = AMEBA_BUILD

    @Inject
    IOSVariantsConfiguration variantsConf

    @TaskAction
    void copyMobileProvision() {
        def userHome = System.getProperty('user.home')
        def mobileProvisionDir = "$userHome/Library/MobileDevice/Provisioning Profiles/"
        new File(mobileProvisionDir).mkdirs()
        variantsConf.variants.each { v ->
            ant.copy(file: v.plist, todir: mobileProvisionDir, overwrite: true)
        }
    }
}
