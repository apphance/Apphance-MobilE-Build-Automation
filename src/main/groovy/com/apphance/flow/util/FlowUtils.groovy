package com.apphance.flow.util

import groovy.io.FileType
import org.gradle.api.logging.Logging

import static com.apphance.flow.util.file.FileManager.MAX_RECURSION_LEVEL
import static com.google.common.base.Preconditions.checkNotNull
import static java.io.File.createTempFile

class FlowUtils {

    private def logger = Logging.getLogger(this.class)

    List<File> allFiles(Map options) {
        def files = []
        File dir = options.dir
        if (dir.exists()) {
            Closure where = options.where ?: { true }
            dir.traverse(type: FileType.FILES, maxDepth: MAX_RECURSION_LEVEL, filter: where) {
                files << it
            }
        }
        files
    }

    boolean equalsIgnoreWhitespace(String left, String right) {
        removeWhitespace(left).equals(removeWhitespace(right))
    }

    String removeWhitespace(String input) {
        input.replaceAll(/\s/, '')
    }

    File downloadToTempFile(String url) {
        File file = tempFile
        logger.info "Downloading $url to file: $file.absolutePath"
        file.append(url.toURL().newInputStream())
        assert file.size() > 0, "Empty downloaded file: $url"
        file
    }

    File unzip(File zip, File destDir) {
        new AntBuilder().unzip(src: zip.absolutePath, dest: destDir.absolutePath)
        destDir
    }

    File getTempFile(String suffix = 'suffix') {
        File file = createTempFile('prefix', suffix)
        file.deleteOnExit()
        file
    }

    //gradle guava 11 workaround
    String getNameWithoutExtension(String file) {
        checkNotNull(file)
        String fileName = new File(file).getName()
        int dotIndex = fileName.lastIndexOf('.')
        (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex)
    }

    String getPackage(File javaFile) {
        def packageRegex = /\s*package\s+(\S+)\s*;.*/
        javaFile.readLines().collect {
            def matcher = it =~ packageRegex
            matcher.matches() ? matcher[0][1] : ''
        }.find() ?: ''
    }
}
