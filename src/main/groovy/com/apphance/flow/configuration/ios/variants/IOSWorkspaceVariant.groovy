package com.apphance.flow.configuration.ios.variants

import com.apphance.flow.plugins.ios.workspace.IOSWorkspaceLocator
import com.google.inject.assistedinject.Assisted

import javax.inject.Inject

import static com.google.common.base.Preconditions.checkNotNull
import static java.text.MessageFormat.format

class IOSWorkspaceVariant extends AbstractIOSVariant {

    @Inject IOSVariantsConfiguration variantsConf
    @Inject IOSWorkspaceLocator workspaceLocator

    private String scheme
    private String workspace

    @Inject
    IOSWorkspaceVariant(@Assisted String name) {
        super(name)
        def tuple = variantsConf.workspaceXscheme.find { w, s -> "$w$s".toString() == name }
        checkNotNull(tuple, format(bundle.getString('exception.ios.variant.workspace.init'), name, variantsConf.workspaceXscheme))
        scheme = tuple[0]
        workspace = tuple[1]
    }

    @Lazy
    List<String> xcodebuildExecutionPath = {
        ['xcodebuild', '-workspace', workspaceFile.name]
    }()

    File getWorkspaceFile() {
        null//TODO
    }
}
