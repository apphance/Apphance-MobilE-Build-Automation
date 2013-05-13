package com.apphance.ameba.configuration.ios.variants

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
        new File(conf.rootDir, pbxJsonParser.plistForTC(target, configuration))
    }

    @Override
    String getBuildableName() {
        throw new UnsupportedOperationException('to be done')
    }

    String getTarget() {
        this.@target
    }

    String getConfiguration() {
        this.@configuration
    }

    @Override
    List<String> buildCmd() {
        conf.xcodebuildExecutionPath() + " -target $target -configuration $configuration ${sdkCmd()}".split()
    }
}
