package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.plugins.ios.parsers.MobileProvisionParser
import com.google.common.base.Preconditions
import groovy.transform.PackageScope
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD
import static java.text.MessageFormat.format
import static java.util.ResourceBundle.getBundle

class CopyMobileProvisionTask extends DefaultTask {

    static final NAME = 'copyMobileProvision'
    String description = "Copies all *.mobileprovision files to user's provisioning profiles folder. While copying it" +
            " checks if bundle identifier in mobileprovision file and in plist are same. If not an exception is thrown."
    String group = FLOW_BUILD

    @Inject IOSVariantsConfiguration variantsConf
    @Inject MobileProvisionParser mpParser

    def bundle = getBundle('validation')

    @TaskAction
    void copyMobileProvision() {
        def mobileProvisionDir = "${System.getProperty('user.home')}/Library/MobileDevice/Provisioning Profiles/"
        new File(mobileProvisionDir).mkdirs()
        variantsConf.variants.findAll { it.mode.value == DEVICE }.each { v ->
            def mobileprovision = v.mobileprovision.value
            validateBundleId(v, mobileprovision)
            ant.copy(file: mobileprovision.absolutePath, todir: mobileProvisionDir, overwrite: true, failonerror: true, verbose: true)
        }
    }

    @PackageScope
    void validateBundleId(AbstractIOSVariant v, File mobileprovision) {
        def plistBID = v.bundleId
        def mobileprovisionBID = mpParser.bundleId(mobileprovision)
        Preconditions.checkState(
                bundleIDsAreEqual(plistBID, mobileprovisionBID) || wildcardBundleIDsMatch(plistBID, mobileprovisionBID),
                format(bundle.getString('exception.ios.bundleId'), v.name, plistBID, mobileprovision.absolutePath, mobileprovisionBID))
    }

    @PackageScope
    boolean bundleIDsAreEqual(String plistBID, String mobileprovisionBID) {
        Preconditions.checkNotNull(plistBID, 'Bundle ID from plist is null')
        Preconditions.checkNotNull(mobileprovisionBID, 'Bundle ID from mobileprovision is null')
        plistBID == mobileprovisionBID
    }

    @PackageScope
    boolean wildcardBundleIDsMatch(String plistBID, String mobileprovisionBID) {
        Preconditions.checkNotNull(plistBID, 'Bundle ID from plist is null')
        Preconditions.checkNotNull(mobileprovisionBID, 'Bundle ID from mobileprovision is null')
        mobileprovisionBID.endsWith('*') && plistBID.startsWith(mobileprovisionBID - '*')
    }
}
