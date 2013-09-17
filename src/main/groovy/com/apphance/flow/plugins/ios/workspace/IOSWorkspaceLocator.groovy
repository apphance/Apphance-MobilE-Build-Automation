package com.apphance.flow.plugins.ios.workspace

import com.apphance.flow.configuration.ios.IOSConfiguration
import org.gradle.api.GradleException

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

    File findWorkspace(String name) {
        findWorkspaceC.call(name)
    }

    private Closure<File> findWorkspaceC = { String name ->
        logger.info("Searching for workspace with name: $name in $conf.rootDir.absolutePath")
        List<File> found = []
        conf.rootDir.traverse(
                type: DIRECTORIES,
                maxDepth: 1,
                nameFilter: ~/${name}\.xcworkspace/,
                excludeFilter: EXCLUDE_FILTER
        ) {
            def contents = new File(it, 'contents.xcworkspacedata')
            if (contents.exists() && isWorkspace.call(contents) && !containsXcodeprojInPath(it)) found << it
        }
        logger.debug("Found following workspaces for name: $name, schemes: $found")
        switch (found.size()) {
            case 0:
                logger.warn("No workspace file found for name: $name")
                return new File(conf.rootDir, "${name}.xcworkspace")
            case 1:
                logger.info("Found workspace file for name: $name, file: ${found[0].absolutePath}")
                return found[0]
            default:
                throw new GradleException("Found more than one workspace file for name: $name, files: $found")
        }
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

    @Lazy
    boolean hasWorkspaces = {
        workspaces.size() > 0
    }()
}
