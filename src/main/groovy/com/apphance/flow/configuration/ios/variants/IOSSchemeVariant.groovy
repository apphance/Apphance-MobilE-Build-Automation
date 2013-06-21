package com.apphance.flow.configuration.ios.variants

import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.google.inject.assistedinject.Assisted

import javax.inject.Inject

class IOSSchemeVariant extends AbstractIOSVariant {

    @Inject XCSchemeParser schemeParser

    @Inject
    IOSSchemeVariant(@Assisted String name) {
        super(name)
    }

    @Override
    File getPlist() {
        String confName = schemeParser.configurationName(name)
        String blueprintId = schemeParser.blueprintIdentifier(name)
        new File(tmpDir, pbxJsonParser.plistForScheme(confName, blueprintId))
    }

    @Override
    String getTarget() {
        pbxJsonParser.targetForBlueprintId(schemeParser.blueprintIdentifier(name))
    }

    @Override
    String getConfiguration() {
        schemeParser.configurationName(name)
    }

    @Override
    List<String> buildCmd() {
        conf.xcodebuildExecutionPath() + ['-scheme', "$name", '-configuration', "$configuration"] + sdkCmd + archCmd + [buildDirCmd]
    }

    @Override
    void checkProperties() {
        super.checkProperties()
    }
}
