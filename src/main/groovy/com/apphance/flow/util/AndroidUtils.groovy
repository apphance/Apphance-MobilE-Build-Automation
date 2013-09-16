package com.apphance.flow.util

import static com.apphance.flow.util.file.FileManager.asProperties

class AndroidUtils {

    List<File> findLibraries(File root) {
        def propFile = new File(root, 'project.properties')
        def references = asProperties(propFile).findAll { it.key.startsWith('android.library.reference') }
        def libNames = references.collect { it.value } as List<String>
        libNames.collect { new File(root, it) }.findAll { it.exists() }
    }

    List<File> allLibraries(File projectRoot) {
        List<File> libraryProjects = findLibraries(projectRoot)
        libraryProjects + libraryProjects.collect { allLibraries(it) }.flatten()
    }
}
