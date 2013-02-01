package com.apphance.ameba.util.file

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class FileDownloader {

    private static Logger l = Logging.getLogger(FileDownloader.class)

    public static void downloadFile(URL url, File file) {
        l.info("Downloading file from ${url} to ${file}")
        def stream = new FileOutputStream(file)
        def out = new BufferedOutputStream(stream)
        out << url.openStream()
        out.close()
    }
}
