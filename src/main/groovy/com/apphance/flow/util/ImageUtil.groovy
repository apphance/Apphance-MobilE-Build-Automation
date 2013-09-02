package com.apphance.flow.util

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

import static org.gradle.api.logging.Logging.getLogger

class ImageUtil {

    private static logger = getLogger(getClass())

    public static BufferedImage getImageFrom(File file) {
        logger.info("Reading file: $file.absolutePath")
        try {
            ImageIO.read(file)
        } catch (Exception ex) {
            logger.error "Error during file read: $ex.message"
            null
        }
    }

}
