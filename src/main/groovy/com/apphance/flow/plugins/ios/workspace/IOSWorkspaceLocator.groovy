package com.apphance.flow.plugins.ios.workspace

import com.apphance.flow.configuration.ios.IOSConfiguration

import javax.inject.Inject

import static com.apphance.flow.util.file.FileManager.EXCLUDE_FILTER
import static groovy.io.FileType.DIRECTORIES
import static org.gradle.api.logging.Logging.getLogger

class IOSWorkspaceLocator {

    private logger = getLogger(getClass())

    @Inject IOSConfiguration conf

    Set<File> getWorkspaces() {
        workspacesC.call()
    }

    private Closure<Set<File>> workspacesC = {
        logger.info("Searching for workspace files in: $conf.rootDir.absolutePath")
        def found = [] as Set<File>
        conf.rootDir.traverse(
                type: DIRECTORIES,
                maxDepth: 1,
                nameFilter: ~/.*\.xcworkspace/,
                excludeFilter: EXCLUDE_FILTER
        ) {
            def contents = new File(it, 'contents.xcworkspacedata')
            if (contents.exists() && isWorkspace.call(contents) && !containsXcodeprojInPath(it)) found << it
        }
        logger.debug("Found workspaces: $found in $conf.rootDir.absolutePath")
        found
    }.memoize()

    private Closure<Boolean> isWorkspace = { File f ->
        try {
            def xml = new XmlSlurper().parse(f)
            return xml.name() == 'Workspace'
        } catch (e) {}
        false
    }.memoize()

    private boolean containsXcodeprojInPath(File file) {
        file.absolutePath.contains('.xcodeproj')
    }
}
