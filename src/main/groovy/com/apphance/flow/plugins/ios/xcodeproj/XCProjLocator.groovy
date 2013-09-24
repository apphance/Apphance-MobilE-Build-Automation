package com.apphance.flow.plugins.ios.xcodeproj

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.plugins.ios.parsers.PbxJsonParser
import org.gradle.api.GradleException

import javax.inject.Inject

import static com.apphance.flow.util.file.FileManager.EXCLUDE_FILTER
import static com.apphance.flow.util.file.FileManager.MAX_RECURSION_LEVEL
import static groovy.io.FileType.DIRECTORIES
import static org.gradle.api.logging.Logging.getLogger

class XCProjLocator {

    public static final PROJECT_PBXPROJ = 'project.pbxproj'

    private logger = getLogger(getClass())

    @Inject IOSConfiguration conf
    @Inject PbxJsonParser pbxParser

    File findXCodeproj(String name, String blueprintId) {
        xcodeprojC.call(name, blueprintId)
    }

    private Closure<File> xcodeprojC = { String name, String blueprintId ->
        logger.info("Searching xcodeproj folder for name: $name and with target ID: $blueprintId")
        List<File> found = []
        conf.rootDir.traverse(
                type: DIRECTORIES,
                maxDepth: MAX_RECURSION_LEVEL,
                nameFilter: ~/$name/,
                excludeFilter: EXCLUDE_FILTER
        ) {
            def pbx = new File(it, PROJECT_PBXPROJ)
            if (pbx.exists()) {
                logger.debug("Found xcodeproj: $pbx.absolutePath")
                def target = pbxParser.targetForBlueprintId.call(pbx, blueprintId)
                logger.debug("Found target: $target in xcodeproj: $pbx.absolutePath and blueprintId: $blueprintId")
                if (target) found << it
            }
        }
        logger.debug("Found following xcodeprojs for name: $name and blueprintId: $blueprintId, xcodeprojs: $found")
        switch (found.size()) {
            case 1:
                return found[0]
            default:
                throw new GradleException("Impossible to find unique xcodeproj for name: $name and blueprintId: $blueprintId, found: $found")
        }
    }.memoize()
}
