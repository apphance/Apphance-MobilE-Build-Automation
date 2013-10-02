package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.plugins.ios.parsers.MobileProvisionParser
import com.apphance.flow.util.Preconditions
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD
import static java.text.MessageFormat.format
import static java.util.ResourceBundle.getBundle

@Mixin(Preconditions)
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

    void validateBundleId(AbstractIOSVariant v, File mobileprovision) {
        validate(v.bundleId == mpParser.bundleId(mobileprovision), {
            throw new GradleException(format(bundle.getString('exception.ios.bundleId'), v.name, v.bundleId, mobileprovision.absolutePath, mpParser.bundleId(mobileprovision)))
        })
    }
}
