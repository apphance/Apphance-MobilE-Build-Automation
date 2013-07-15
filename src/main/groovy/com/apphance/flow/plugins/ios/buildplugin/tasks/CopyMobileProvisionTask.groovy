package com.apphance.flow.plugins.ios.buildplugin.tasks

import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.plugins.ios.parsers.MobileProvisionParser
import com.apphance.flow.util.Preconditions
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD

@Mixin(Preconditions)
class CopyMobileProvisionTask extends DefaultTask {

    static final NAME = 'copyMobileProvision'
    String description = 'Copies mobile provision file to the user library'
    String group = FLOW_BUILD

    @Inject IOSVariantsConfiguration variantsConf
    @Inject MobileProvisionParser mpParser

    @TaskAction
    void copyMobileProvision() {
        def userHome = System.getProperty('user.home')
        def mobileProvisionDir = "$userHome/Library/MobileDevice/Provisioning Profiles/"
        new File(mobileProvisionDir).mkdirs()
        //TODO add checking 'archive' action as well as 'build'
        variantsConf.variants.each { v ->
            def mobileprovision = v.mobileprovision.value
            validateBundleId(v, mobileprovision)
            ant.copy(file: mobileprovision.absolutePath, todir: mobileProvisionDir, overwrite: true, failonerror: true, verbose: true)
        }
    }

    private void validateBundleId(IOSVariant v, File mobileprovision) {
        validate(v.bundleId == mpParser.bundleId(mobileprovision), {
            throw new GradleException("""|Bundle Id from variant: ${v.name} (${v.bundleId})
                                         |and from mobile provision file: ${mobileprovision.absolutePath}
                                         |(${mpParser.bundleId(mobileprovision)}) do not match!""".stripMargin())
        })
    }
}
