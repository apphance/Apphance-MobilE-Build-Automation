package com.apphance.flow.configuration.ios.variants

import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.google.inject.assistedinject.Assisted

import javax.inject.Inject

@com.google.inject.Singleton
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
        new File(tmpDir, pbxJsonParser.plistForScheme(variantPbx, confName, blueprintId))
    }

    @Override
    String getTarget() {
        pbxJsonParser.targetForBlueprintId(variantPbx, schemeParser.blueprintIdentifier(name))
    }

    @Override
    String getConfiguration() {
        schemeParser.configurationName(name)
    }

    @Override
    List<String> buildCmd() {
        conf.xcodebuildExecutionPath() + ['-scheme', name] + sdkCmd + archCmd + [buildDirCmd]
    }

    String getArchiveTaskName() {
        "archive$name"
    }

    List<String> archiveCmd() {
        conf.xcodebuildExecutionPath() + ['-scheme', name] + sdkCmd + archCmd + [buildDirCmd] + ['archive']
    }

    @Override
    void checkProperties() {
        super.checkProperties()
    }
}
