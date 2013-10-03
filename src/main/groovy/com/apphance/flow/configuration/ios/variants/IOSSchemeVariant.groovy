package com.apphance.flow.configuration.ios.variants

import com.google.inject.assistedinject.Assisted

import javax.inject.Inject

import static com.apphance.flow.util.file.FileManager.relativeTo

/**
 * Variant configuration keeps all variant-related settings, used while building artifacts. Depending on the values set,
 * various artifacts are created. See plugin reference for the details.
 */
class IOSSchemeVariant extends AbstractIOSVariant {

    @Inject
    IOSSchemeVariant(@Assisted String name) {
        super(name)
    }

    @Lazy
    List<String> xcodebuildExecutionPath = {
        ['xcodebuild', '-project', xcodeprojFile.path]
    }()

    private File getXcodeprojFile() {
        def xcodeproj = xcodeprojLocator.findXCodeproj(schemeParser.xcodeprojName(schemeFile, action), schemeParser.blueprintIdentifier(schemeFile, action))
        def relative = relativeTo(conf.rootDir, xcodeproj)
        new File(tmpDir, relative)
    }

    @Override
    String getSchemeName() {
        name
    }
}
