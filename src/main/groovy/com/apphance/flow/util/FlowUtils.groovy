package com.apphance.flow.util

import groovy.io.FileType

import java.nio.file.Files

import static com.apphance.flow.util.file.FileManager.MAX_RECURSION_LEVEL
import static com.google.common.base.Preconditions.checkArgument

class FlowUtils {

    List<File> allFiles(Map options) {
        def files = []
        File dir = options.dir
        checkArgument(dir.exists())

        Closure where = options.where ?: { true }
        dir.traverse(type: FileType.FILES, maxDepth: MAX_RECURSION_LEVEL, filter: where) {
            files << it
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
        File file = Files.createTempFile('tempFile', '.ext').toFile()
        file.append(url.toURL().newInputStream())
        assert file.size() > 0, "Empty downloaded file: $url"
        file
    }

    File unzip(File zip, File destDir) {
        new AntBuilder().unzip(src: zip.absolutePath, dest: destDir.absolutePath)
        destDir
    }
}
