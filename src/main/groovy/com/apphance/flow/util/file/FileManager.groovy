package com.apphance.flow.util.file

import com.google.common.io.Files
import groovy.io.FileType
import org.gradle.api.Project

import java.nio.file.Paths

import static com.apphance.flow.configuration.ProjectConfiguration.BUILD_DIR
import static com.apphance.flow.configuration.ProjectConfiguration.TMP_DIR
import static com.apphance.flow.configuration.release.ReleaseConfiguration.OTA_DIR
import static groovy.io.FileType.FILES
import static java.lang.String.format
import static java.nio.charset.StandardCharsets.UTF_8

class FileManager {
    public static final EXCLUDE_FILTER = ~/.*(${TMP_DIR}|${OTA_DIR}|${BUILD_DIR}|\.git|\.hg).*/
    public static final int MAX_RECURSION_LEVEL = 7
    private static final long MEGABYTE = 1048576L

    public static void removeMissingSymlinks(File baseDirectory) {
        baseDirectory.traverse([type: FILES, maxDepth: MAX_RECURSION_LEVEL]) {
            if (!it.isDirectory()) {
                File canonicalFile = it.getCanonicalFile()
                if (!canonicalFile.exists()) {
                    it.delete()
                }
            }
        }
    }

    public static getHumanReadableSize(long byteSize) {
        byteSize >= MEGABYTE ? format("%.2f", byteSize * 1.0 / 1024.0 / 1024.0) + " MB" : format("%.2f", byteSize * 1.0 / 1024.0) + " kB"
    }

    public static File relativeTo(String from, String to) {
        Paths.get(from).relativize(Paths.get(to)).toFile()
    }

    static String relativeTo(final File base, final File file) {
        base.toPath().relativize(file.toPath())
    }

    static String replace(final File file, final String placeholder, final String replacement) {
        assert file.exists()
        file.text = file.text.replace(placeholder, replacement)
    }

    static Properties asProperties(final File file) {
        def projectProperties = new Properties()
        assert file.exists()
        projectProperties.load Files.newReader(file, UTF_8)
        projectProperties
    }

    static Boolean isAndroidLibrary(final File file) {
        assert file?.exists() && file.isDirectory()
        File propFile = new File(file, 'project.properties')
        propFile.exists() && Boolean.valueOf(asProperties(propFile).getProperty('android.library'))
    }
}
