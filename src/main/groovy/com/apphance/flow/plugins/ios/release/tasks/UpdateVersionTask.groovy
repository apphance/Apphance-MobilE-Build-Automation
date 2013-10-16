package com.apphance.flow.plugins.ios.release.tasks

import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.plugins.ios.parsers.PbxJsonParser
import com.apphance.flow.plugins.ios.parsers.PlistParser
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.apphance.flow.plugins.release.tasks.AbstractUpdateVersionTask

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSBuildMode.FRAMEWORK

class UpdateVersionTask extends AbstractUpdateVersionTask {

    String description = "Updates version stored in configuration file of the project - plist." +
            " Numeric version is set from 'version.code' system property (-D) or 'VERSION_CODE' environment variable " +
            "property. String version is set from 'version.string' system property (-D) or 'VERSION_CODE' " +
            "environment variable."

    @Inject PlistParser parser
    @Inject XCSchemeParser schemeParser
    @Inject PbxJsonParser pbxJsonParser
    @Inject IOSVariantsConfiguration variantsConf

    @Override
    void updateDescriptor(String versionCode, String versionString) {
        variantsConf.variants.findAll { it.mode.value != FRAMEWORK }.collect { v ->
            def blueprintId = schemeParser.blueprintIdentifier(v.schemeFile)
            new File(conf.rootDir, pbxJsonParser.plistForScheme.call(v.pbxFile, v.archiveConfiguration, blueprintId))
        }.unique().each {
            logger.info("Updating plist: $it.absolutePath, versionCode: $versionCode, versionString: $versionString")
            parser.replaceVersion(it, versionCode, versionString)
        }
    }
}
