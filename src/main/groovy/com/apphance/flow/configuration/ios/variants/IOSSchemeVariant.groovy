package com.apphance.flow.configuration.ios.variants

import com.google.inject.assistedinject.Assisted

import javax.inject.Inject

class IOSSchemeVariant extends AbstractIOSVariant {

    @Inject
    IOSSchemeVariant(@Assisted String name) {
        super(name)
    }

    @Lazy
    List<String> xcodebuildExecutionPath = {
        ['xcodebuild', '-project', xcodeprojLocator.findXCodeproj(schemeParser.xcodeprojName(schemeFile), schemeParser.blueprintIdentifier(schemeFile)).name]
    }()
}
