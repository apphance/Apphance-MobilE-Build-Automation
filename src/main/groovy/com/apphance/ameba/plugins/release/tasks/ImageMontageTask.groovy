package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.plugins.release.AmebaArtifact
import ij.ImagePlus
import ij.ImageStack
import ij.plugin.MontageMaker
import ij.process.ColorProcessor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.imageio.ImageIO
import javax.inject.Inject
import java.awt.*
import java.awt.image.BufferedImage
import java.util.List

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static com.apphance.ameba.util.file.FileManager.MAX_RECURSION_LEVEL
import static groovy.io.FileType.FILES
import static org.gradle.api.logging.Logging.getLogger
import static org.imgscalr.Scalr.pad

@Mixin(ImageNameFilter)
class ImageMontageTask extends DefaultTask {

    def log = getLogger(this.class)

    static String NAME = 'prepareImageMontage'
    String group = AMEBA_RELEASE
    String description = 'Builds montage of images found in the project'

    @Inject
    AndroidConfiguration conf
    @Inject
    AndroidReleaseConfiguration releaseConf

    public static int TILE_PX_SIZE = 120
    public static int MAX_NUMBER_OF_TILES_IN_ROW = 10
    public static int DESCRIPTION_FONT_SIZE = 10

    @TaskAction
    void imageMontage() {
        def filesToMontage = getFilesToMontage(project.rootDir)
        File imageMontageFile = outputMontageFile()
        createMontage(imageMontageFile, filesToMontage)
        addDescription(imageMontageFile, "${conf.projectName.value} Version: ${conf.fullVersionString} Generated: ${releaseConf.buildDate}")

        def imageMontageFileArtifact = new AmebaArtifact(
                name: "Image Montage", url: new URL(releaseConf.projectURL.value, "${imageMontageFile.name}"), location: imageMontageFile)
        releaseConf.imageMontageFile = imageMontageFileArtifact
    }

    @groovy.transform.PackageScope
    void addDescription(File image, String description) {
        BufferedImage img = ImageIO.read(image)

        Graphics2D graphics = img.createGraphics();
        graphics.setPaint(Color.BLACK);
        graphics.setFont(new Font("Monospaced", Font.BOLD, DESCRIPTION_FONT_SIZE));
        graphics.drawString(description, 10, 10);
        graphics.dispose();

        ImageIO.write(img, "png", image)
    }

    @groovy.transform.PackageScope
    File outputMontageFile() {
        def imageMontageFile = new File(releaseConf.targetDirectory, "${conf.projectName.value}-${conf.fullVersionString}-image-montage.png")
        imageMontageFile.parentFile.mkdirs()
        imageMontageFile.delete()
        imageMontageFile
    }

    @groovy.transform.PackageScope
    List<File> getFilesToMontage(File rootDir) {
        List<File> filesToMontage = []

        rootDir.traverse([type: FILES, maxDepth: MAX_RECURSION_LEVEL, excludeFilter: '**/ameba-*/**']) { File file ->
            //FIXME apply better filter
            if (isValid(rootDir, file) && [conf.tmpDir, releaseConf.otaDir]*.name.every { !file.absolutePath.contains(it) }) {
                filesToMontage << file
            }
        }
        filesToMontage
    }

    @groovy.transform.PackageScope
    void createMontage(File ouput, List<File> inputs) {
        Collection<Image> images = resizeImages(inputs)

        def processors = images.collect { new ColorProcessor(it) }

        ImageStack imageStack = new ImageStack(TILE_PX_SIZE, TILE_PX_SIZE)
        processors.each { imageStack.addSlice(it) }

        def imgPlus = new ImagePlus("stack", imageStack)
        int columns, rows
        (columns, rows) = computeWidthHeight(images.size())

        ImagePlus montage = new MontageMaker().makeMontage2(imgPlus, columns, rows, getScale(images.size()), 1, images.size(), 1, 0, false)
        ImageIO.write(montage.bufferedImage, "png", ouput);
    }

    private double getScale(int size) {
        switch (size) {
            case 0..200: 1.0D; break
            case 201..800: 0.5D; break
            default: 0.25
        }
    }

    @groovy.transform.PackageScope
    List<Integer> computeWidthHeight(int numberOfTiles) {
        int columns = Math.min(numberOfTiles, MAX_NUMBER_OF_TILES_IN_ROW)
        int rows = Math.ceil(numberOfTiles / columns)
        [columns, rows]
    }

    @groovy.transform.PackageScope
    Collection<Image> resizeImages(List<File> inputs) {
        Collection<Image> images = inputs.collect {

            def image = getImageFrom(it)

            if (image != null) {
                image = pad(image, 20, Color.WHITE)
                image.getScaledInstance(TILE_PX_SIZE, TILE_PX_SIZE, Image.SCALE_DEFAULT)
            } else {
                log.error("Problem during converting ${it.absolutePath}")
            }
        }

        images.removeAll { it == null }
        images
    }

    @groovy.transform.PackageScope
    BufferedImage getImageFrom(File file) {
        log.info("Reading file: $file.absolutePath")
        getConverter(file.name)(file)
    }

    private Closure<BufferedImage> getConverter(String filename) {
        switch (filename) {
            case ~/.*\.svg/: this.&svgConverter; break
            default: ImageIO.&read
        }
    }

    BufferedImage svgConverter(File file) {
        // TODO
        ImageIO.read(file)
    }
}
