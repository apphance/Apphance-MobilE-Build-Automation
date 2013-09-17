package com.apphance.flow.plugins.ios.cocoapods

import static com.apphance.flow.util.file.FileManager.EXCLUDE_FILTER
import static com.apphance.flow.util.file.FileManager.MAX_RECURSION_LEVEL
import static groovy.io.FileType.FILES

class PodLocator {

    Set<File> findPodfile(File dir) {
        findPodfileC.call(dir)
    }

    private Closure<Set<File>> findPodfileC = { File dir ->
        Set<File> podfiles = []
        dir.traverse(
                type: FILES,
                maxDepth: MAX_RECURSION_LEVEL,
                nameFilter: ~/Podfile/,
                excludeFilter: EXCLUDE_FILTER) {
            if (it.isFile() && it.exists() && it.size() > 0) podfiles << it
        }
        podfiles
    }.memoize()
}
