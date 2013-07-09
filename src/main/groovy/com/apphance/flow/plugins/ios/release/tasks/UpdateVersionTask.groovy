package com.apphance.flow.plugins.ios.release.tasks

import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.plugins.ios.parsers.PbxJsonParser
import com.apphance.flow.plugins.ios.parsers.PlistParser
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.apphance.flow.plugins.release.tasks.AbstractUpdateVersionTask

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.variants.IOSXCodeAction.ARCHIVE_ACTION

class UpdateVersionTask extends AbstractUpdateVersionTask {

    @Inject PlistParser parser
    @Inject XCSchemeParser schemeParser
    @Inject IOSVariantsConfiguration variantsConf
    @Inject PbxJsonParser pbxJsonParser

    @Override
    void updateDescriptor(String versionCode, String versionString) {
        def plists = variantsConf.variants.collect { v ->
            def blueprintId = schemeParser.blueprintIdentifier(v.schemeFile)
            def conf = schemeParser.configuration(v.schemeFile, ARCHIVE_ACTION)
            new File(v.tmpDir, pbxJsonParser.plistForScheme(v.variantPbx, conf, blueprintId))
        }.unique()
        plists.each {
            logger.info("Updating plist: $it.absolutePath, versionCode: $versionCode, versionString: $versionString")
            parser.replaceVersion(it, versionCode, versionString)
        }
    }
}
