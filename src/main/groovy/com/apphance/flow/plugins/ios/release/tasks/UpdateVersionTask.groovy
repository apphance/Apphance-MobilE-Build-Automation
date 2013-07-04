package com.apphance.flow.plugins.ios.release.tasks

import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.plugins.ios.parsers.PlistParser
import com.apphance.flow.plugins.release.tasks.AbstractUpdateVersionTask

import javax.inject.Inject

class UpdateVersionTask extends AbstractUpdateVersionTask {

    @Inject PlistParser parser
    @Inject IOSVariantsConfiguration variantsConf

    @Override
    void updateDescriptor(String versionCode, String versionString) {
        variantsConf.variants*.plist.unique().each {
            parser.replaceVersion(it, versionCode, versionString)
        }
    }
}
