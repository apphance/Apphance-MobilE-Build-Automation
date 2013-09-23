package com.apphance.flow.plugins.ios.cocoapods

import static com.apphance.flow.util.file.FileManager.EXCLUDE_FILTER
import static com.apphance.flow.util.file.FileManager.MAX_RECURSION_LEVEL
import static groovy.io.FileType.FILES
import static org.gradle.api.logging.Logging.getLogger

class PodLocator {

    private logger = getLogger(getClass())

    File findPodfile(File dir) {
        findPodfileC.call(dir)
    }

    private Closure<File> findPodfileC = { File dir ->
        File podfile = null
        logger.info("Looking for Podfile in: $dir.absolutePath")
        dir.traverse(
                type: FILES,
                maxDepth: MAX_RECURSION_LEVEL,
                nameFilter: ~/Podfile/,
                excludeFilter: EXCLUDE_FILTER) {
            if (it.isFile() && it.exists() && it.size() > 0) podfile = it
        }
        logger.info("Found Podfile: $podfile in dir $dir.absolutePath")
        podfile
    }.memoize()
}
