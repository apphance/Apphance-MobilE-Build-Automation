package com.apphance.ameba.plugins.release.tasks
/**
 * Filter for image names.
 *
 */
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

    private final String[] INVALID_PREFIXES = [
            'build/',
            'bin/',
            'doc/',
            'log/',
            'documentation/',
            'ota/',
            'tmp/',
            'External/'
    ]

    private final String[] INVALID_DIRS = [
            '/Shared/External/'
    ]

    final boolean isValid(File rootDirectory, File file) {
        if ((VALID_EXTENSIONS.findAll { file.name.toLowerCase().endsWith(it) }).size() == 0) {
            return false
        }
        if (INVALID_PREFIXES.findAll { file.toString().startsWith(new File(rootDirectory, it).toString()) }.size() > 0) {
            return false
        }
        if (INVALID_DIRS.findAll { file.toString().contains(it) }.size() > 0) {
            return false
        }
        return true
    }
}
