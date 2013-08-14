package com.apphance.flow.util.file

import static org.gradle.api.logging.Logging.getLogger

class FileDownloader {

    private static logger = getLogger(FileDownloader.class)

    public static void downloadFile(URL url, File file) {
        logger.info("Downloading file from ${url} to ${file}")
        def stream = new FileOutputStream(file)
        def out = new BufferedOutputStream(stream)
        out << url.openStream()
        out.close()
    }
}
