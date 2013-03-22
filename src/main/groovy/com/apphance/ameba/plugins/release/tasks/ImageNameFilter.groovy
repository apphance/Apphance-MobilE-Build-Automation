package com.apphance.ameba.plugins.release.tasks
/**
 * Filter for image names.
 *
 */
class ImageNameFilter {

    private String[] validExtensions = [
            ".jpg",
            ".jpeg",
            ".exif",
            ".png",
            ".bmp",
            ".gif",
            ".tif",
            ".tiff",
            ".raw",
            ".svg",
            ".webp"
    ]

    private String[] invalidPrefixes = [
            "build/",
            "bin/",
            "doc/",
            "log/",
            "documentation/",
            "ota/",
            "tmp/",
            "External/"
    ]

    private String[] invalidDirectories = [
            "/Shared/External/"
    ]


    boolean isValid(File rootDirectory, File file) {
        def lowerCaseName = file.name.toLowerCase()
        if ((validExtensions.findAll { lowerCaseName.endsWith(it) }).size() == 0) {
            return false
        }
        if (invalidPrefixes.findAll { file.toString().startsWith(new File(rootDirectory, it).toString()) }.size() > 0) {
            return false
        }
        if (invalidDirectories.findAll { file.toString().contains(it) }.size() > 0) {
            return false
        }
        return true
    }
}
