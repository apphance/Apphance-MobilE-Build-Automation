package com.apphance.flow.configuration.ios.variants

import com.google.inject.assistedinject.Assisted

import javax.inject.Inject

class IOSTCVariant extends AbstractIOSVariant {

    private String target
    private String configuration

    @Inject
    IOSTCVariant(@Assisted String name) {
        super(name)
    }

    @Inject
    @Override
    void init() {
        List<String> tuple = conf.targetConfigurationMatrix.find {
            "${it[0]}${it[1]}" == name
        }
        target = tuple[0]
        configuration = tuple[1]
        super.init()
    }

    @Override
    File getPlist() {
        new File(tmpDir, pbxJsonParser.plistForTC(target, configuration))
    }

    @Override
    String getTarget() {
        this.@target
    }

    @Override
    String getConfiguration() {
        this.@configuration
    }

    @Override
    List<String> buildCmd() {
        conf.xcodebuildExecutionPath() + ['-target', "$target", '-configuration', "$configuration"] + sdkCmd + archCmd + [buildDirCmd]
    }

    @Override
    void checkProperties() {
        super.checkProperties()
    }
}
