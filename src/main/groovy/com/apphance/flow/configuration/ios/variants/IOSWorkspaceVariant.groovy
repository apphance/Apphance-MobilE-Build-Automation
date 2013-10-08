package com.apphance.flow.configuration.ios.variants

import com.apphance.flow.plugins.ios.workspace.XCWorkspaceLocator
import com.google.inject.assistedinject.Assisted

import javax.inject.Inject

import static com.apphance.flow.util.file.FileManager.relativeTo
import static com.google.common.base.Preconditions.checkNotNull
import static java.text.MessageFormat.format

class IOSWorkspaceVariant extends AbstractIOSVariant {

    @Inject IOSVariantsConfiguration variantsConf
    @Inject XCWorkspaceLocator workspaceLocator

    private String workspaceName
    private String schemeName

    @Inject
    IOSWorkspaceVariant(@Assisted String name) {
        super(name)
    }

    @Inject
    @Override
    void init() {
        super.init()
        def tuple = variantsConf.workspaceXscheme.find { w, s -> "$w$s".toString() == name }
        checkNotNull(tuple, format(validationBundle.getString('exception.ios.variant.workspace.init'), name, variantsConf.workspaceXscheme.collect({ w, s -> "$w+$s" })))
        workspaceName = tuple[0]
        schemeName = tuple[1]
    }

    @Lazy
    List<String> xcodebuildExecutionPath = {
        ['xcodebuild', '-workspace', workspaceFile.path]
    }()

    File getWorkspaceFile() {
        def ws = workspaceLocator.findWorkspace(workspaceName)
        def relative = relativeTo(conf.rootDir, ws)
        new File(tmpDir, relative)
    }

    @Override
    String getSchemeName() {
        this.@schemeName
    }
}
