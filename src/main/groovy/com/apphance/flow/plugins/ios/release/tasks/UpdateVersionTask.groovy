package com.apphance.flow.plugins.ios.release.tasks

import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.plugins.ios.parsers.PbxJsonParser
import com.apphance.flow.plugins.ios.parsers.PlistParser
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.apphance.flow.plugins.release.tasks.AbstractUpdateVersionTask

import javax.inject.Inject

class UpdateVersionTask extends AbstractUpdateVersionTask {

    @Inject PlistParser parser
    @Inject XCSchemeParser schemeParser
    @Inject PbxJsonParser pbxJsonParser
    @Inject IOSVariantsConfiguration variantsConf

    @Override
    void updateDescriptor(String versionCode, String versionString) {
        def plists = variantsConf.variants.collect { v ->
            def blueprintId = schemeParser.blueprintIdentifier(v.schemeFile)
            new File(v.tmpDir, pbxJsonParser.plistForScheme(v.variantPbx, v.archiveConfiguration, blueprintId))
        }.unique()
        plists.each {
            logger.info("Updating plist: $it.absolutePath, versionCode: $versionCode, versionString: $versionString")
            parser.replaceVersion(it, versionCode, versionString)
        }
    }
}
