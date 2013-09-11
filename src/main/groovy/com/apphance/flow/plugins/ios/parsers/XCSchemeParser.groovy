package com.apphance.flow.plugins.ios.parsers

import com.apphance.flow.configuration.ios.variants.IOSXCodeAction
import com.apphance.flow.util.Preconditions
import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil
import org.gradle.api.GradleException

import static org.gradle.api.logging.Logging.getLogger

@Mixin(Preconditions)
class XCSchemeParser {

    private logger = getLogger(getClass())

    private static final POST_ARCHIVE_ACTION_ECHO_DIR = '''
    <ExecutionAction ActionType = "Xcode.IDEStandardExecutionActionsCore.ExecutionActionType.ShellScriptAction">
        <ActionContent title = "ECHO_FLOW_ARCHIVE_PATH" scriptText = "echo &quot;\\nFLOW_ARCHIVE_PATH=$ARCHIVE_PATH\\n&quot;">
        </ActionContent>
    </ExecutionAction>
'''

    String configuration(File scheme, IOSXCodeAction action) {
        configurationC.call(scheme, action)
    }

    @Lazy
    private Closure<String> configurationC = { File scheme, IOSXCodeAction action ->
        def xml = parseSchemeFile.call(scheme)
        xml."$action.xmlNodeName".@buildConfiguration.text()
    }.memoize()

    boolean hasSingleBuildableTarget(File scheme) {
        def xml
        try { xml = parseSchemeFile.call(scheme) } catch (e) { return false }
        xml.BuildAction.BuildActionEntries.children().size() == 1
    }

    boolean isBuildable(File scheme) {
        def xml
        try { xml = parseSchemeFile.call(scheme) } catch (e) { return false }
        xml.LaunchAction.BuildableProductRunnable.size() != 0
    }

    boolean hasEnabledTestTargets(File scheme) {
        def xml
        try { xml = parseSchemeFile.call(scheme) } catch (e) { return false }
        def skipped = xml.TestAction.Testables.TestableReference.findAll { it.@skipped.text() == 'YES' }.size()
        def enabled = xml.TestAction.Testables.TestableReference.findAll { it.@skipped.text() == 'NO' }.size()
        logger.info("$enabled/${enabled + skipped} tests enabled in scheme: $scheme.absolutePath")
        enabled > 0
    }

    List<String> findActiveTestableBlueprintIds(File scheme) {
        def xml = parseSchemeFile.call(scheme)
        xml.TestAction.Testables.TestableReference.findAll {
            it.@skipped.text() == 'NO'
        }*.BuildableReference*.@BlueprintIdentifier*.text()
    }

    String blueprintIdentifier(File scheme) {
        blueprintIdentifierC.call(scheme)
    }

    @Lazy
    private Closure<String> blueprintIdentifierC = { File scheme ->
        def xml = parseSchemeFile.call(scheme)
        xml.LaunchAction.BuildableProductRunnable.BuildableReference.@BlueprintIdentifier
    }.memoize()


    String xcodeprojName(File scheme) {
        xcodeprojNameC.call(scheme)
    }

    private Closure<String> xcodeprojNameC = { File scheme ->
        def xml = parseSchemeFile.call(scheme)
        def ref = xml.LaunchAction.BuildableProductRunnable.BuildableReference.@ReferencedContainer
        def splitted = ref?.text()?.split(':')
        splitted?.size() == 2 ? splitted[1] : null
    }.memoize()

    void addPostArchiveAction(File scheme) {
        def xml = parseSchemeFile.call(scheme)
        if (xml.ArchiveAction.PostActions.size() == 0) {
            xml.ArchiveAction.appendNode { PostActions() }
            xml = new XmlSlurper().parseText(XmlUtil.serialize(xml))
        }
        xml.ArchiveAction.PostActions.appendNode(new XmlSlurper().parseText(POST_ARCHIVE_ACTION_ECHO_DIR))
        xml.ArchiveAction.@revealArchiveInOrganizer = 'YES'
        scheme.text = XmlUtil.serialize(xml)
    }

    private Closure<GPathResult> parseSchemeFile = { File scheme ->
        validate(scheme?.exists() && scheme?.isFile() && scheme?.size() > 0, {
            throw new GradleException("Schemes must be shared! Invalid scheme file: ${scheme?.absolutePath}")
        })
        new XmlSlurper().parse(scheme)
    }.memoize()
}
