package com.apphance.flow.util

import groovy.io.FileType

import static com.google.common.base.Preconditions.*

class FlowUtils {

    List<File> allFiles(Map options) {
        def files = []
        File dir = options.dir
        checkArgument(dir.exists())

        Closure where = options.where ?: { true }
        dir.traverse(type: FileType.FILES, filter: where) {
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
}
