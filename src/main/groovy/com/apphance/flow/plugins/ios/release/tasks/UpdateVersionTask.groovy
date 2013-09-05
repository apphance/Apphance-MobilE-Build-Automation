package com.apphance.flow.plugins.ios.release.tasks

import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.plugins.ios.parsers.PbxJsonParser
import com.apphance.flow.plugins.ios.parsers.PlistParser
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.apphance.flow.plugins.release.tasks.AbstractUpdateVersionTask

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSBuildMode.FRAMEWORK

class UpdateVersionTask extends AbstractUpdateVersionTask {

    @Inject PlistParser parser
    @Inject XCSchemeParser schemeParser
    @Inject PbxJsonParser pbxJsonParser
    @Inject IOSVariantsConfiguration variantsConf

    @Override
    void updateDescriptor(String versionCode, String versionString) {
        variantsConf.variants.findAll { it.mode.value != FRAMEWORK }.collect { v ->
            def blueprintId = schemeParser.blueprintIdentifier(v.schemeFile)
            new File(v.tmpDir, pbxJsonParser.plistForScheme.call(v.pbxFile, v.archiveConfiguration, blueprintId))
        }.unique().each {
            logger.info("Updating plist: $it.absolutePath, versionCode: $versionCode, versionString: $versionString")
            parser.replaceVersion(it, versionCode, versionString)
        }
    }
}
