package com.apphance.flow.plugins.release.tasks

class ImageNameFilter {

    private final String[] VALID_EXTENSIONS = [
            '.jpg',
            '.jpeg',
            '.exif',
            '.png',
            '.bmp',
            '.gif',
            '.tif',
            '.tiff',
            '.raw',
            '.svg',
            '.webp'
    ]

    private final String[] INVALID_DIRS = [
            'build/',
            'bin/',
            'doc/',
            'log/',
            'flow-log/',
            'documentation/',
            'flow-ota/',
            'flow-tmp/',
            'tmp/',
            'External/',
            '/Shared/External/'
    ]

    final boolean isValid(File file) {
        if (file == null || !file?.exists()) {
            return false
        }
        if ((VALID_EXTENSIONS.findAll { file.name.toLowerCase().endsWith(it) }).size() == 0) {
            return false
        }
        if (INVALID_DIRS.findAll { file.absolutePath.contains(it) }.size() > 0) {
            return false
        }
        return true
    }
}
