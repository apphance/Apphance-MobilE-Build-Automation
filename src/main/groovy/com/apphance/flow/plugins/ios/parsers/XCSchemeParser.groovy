package com.apphance.flow.plugins.ios.parsers

import com.apphance.flow.configuration.ios.variants.IOSXCodeAction
import com.apphance.flow.util.Preconditions
import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil
import org.gradle.api.GradleException

@Mixin(Preconditions)
class XCSchemeParser {

    private static final POST_ARCHIVE_ACTION_ECHO_DIR = '''
    <ExecutionAction ActionType = "Xcode.IDEStandardExecutionActionsCore.ExecutionActionType.ShellScriptAction">
        <ActionContent title = "ECHO_FLOW_ARCHIVE_PATH" scriptText = "echo &quot;\\nFLOW_ARCHIVE_PATH=$ARCHIVE_PATH\\n&quot;">
        </ActionContent>
    </ExecutionAction>
'''

    String configuration(File scheme, IOSXCodeAction action) {
        def xml = parseSchemeFile(scheme)
        xml."$action.xmlNodeName".@buildConfiguration.text()
    }

    boolean isBuildable(File scheme) {
        def xml
        try { xml = parseSchemeFile(scheme) } catch (e) { return false }
        xml.LaunchAction.BuildableProductRunnable.size() != 0
    }

    String blueprintIdentifier(File scheme) {
        def xml = parseSchemeFile(scheme)
        def blueprintIdentifier = xml.LaunchAction.BuildableProductRunnable.BuildableReference.@BlueprintIdentifier
        blueprintIdentifier
    }

    void addPostArchiveAction(File scheme) {
        def xml = parseSchemeFile(scheme)
        if (xml.ArchiveAction.PostActions.size() == 0) {
            xml.ArchiveAction.appendNode { PostActions() }
            xml = new XmlSlurper().parseText(XmlUtil.serialize(xml))
        }
        xml.ArchiveAction.PostActions.appendNode(new XmlSlurper().parseText(POST_ARCHIVE_ACTION_ECHO_DIR))
        xml.ArchiveAction.@revealArchiveInOrganizer = 'YES'
        scheme.text = XmlUtil.serialize(xml)
    }

    private GPathResult parseSchemeFile(File scheme) {
        validate(scheme.exists() && scheme.isFile() && scheme.size() > 0, {
            throw new GradleException("Shemes must be shared! Invalid scheme file: $scheme.absolutePath")
        })
        new XmlSlurper().parse(scheme)
    }
}
