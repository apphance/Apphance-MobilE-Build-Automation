package com.apphance.ameba

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Filter for image names.
 *
 */
class ImageNameFilter {
    static Logger logger = Logging.getLogger(ImageNameFilter.class)
    private static String[] validExtensions = [
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

    private static String[] invalidPrefixes = [
            "build/",
            "bin/",
            "doc/",
            "log/",
            "documentation/",
            "ota/",
            "tmp/",
            "External/"
    ]

    private static String[] invalidDirectories = [
            "/Shared/External/"
    ]


    def static boolean isValid(File rootDirectory, File file) {
        def lowerCaseName = file.name.toLowerCase()
        if ((validExtensions.findAll { lowerCaseName.endsWith(it) }).size == 0) {
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
