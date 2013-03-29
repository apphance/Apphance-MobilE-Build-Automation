package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.executor.command.CommandLogFilesGenerator
import com.apphance.ameba.executor.linker.FileLinker
import com.apphance.ameba.executor.linker.SimpleFileLinker
import com.apphance.ameba.plugins.projectconfiguration.ProjectConfiguration
import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration
import com.google.common.io.Files
import ij.ImagePlus
import org.gradle.api.Project
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import javax.imageio.ImageIO

import static com.apphance.ameba.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.ameba.executor.command.CommandLogFilesGenerator.LogFile.STD

import static java.io.File.createTempFile

class ImageMontageTaskSpec extends Specification {

    def imageMontageTask = new ImageMontageTask()

    private File testMontageFilesDir = new File('src/test/resources/com/apphance/ameba/plugins/release/tasks/montageFiles')
    def fileForDescTest = new File('src/test/resources/com/apphance/ameba/plugins/release/tasks/Blank.jpg')

    def setup() {
        Project project = Mock()
        ProjectConfiguration conf = Mock()
        ProjectReleaseConfiguration releaseConf = Mock()
        FileLinker fileLinker = new SimpleFileLinker()
        CommandLogFilesGenerator logFileGenerator = Mock()
        CommandExecutor commandExecutor = new CommandExecutor(fileLinker, logFileGenerator)

        imageMontageTask.project = project
        imageMontageTask.executor = commandExecutor
        imageMontageTask.conf = conf
        imageMontageTask.releaseConf = releaseConf

        def testDir = Files.createTempDir()
        testDir.deleteOnExit()
        releaseConf.targetDirectory >> testDir
        conf.projectName >> 'testProjectName'
        conf.fullVersionString >> 'fullVersionString'
        project.rootDir >> new File(System.getProperty("user.dir"))
        logFileGenerator.commandLogFiles() >> [(ERR): createTempFile('err', 'log'), (STD): createTempFile('std', 'log')]
        0 * _
    }

    def "test outputMontageFile"() {
        when:
        def file = imageMontageTask.outputMontageFile()

        then:
        !file.exists()
        file.name == 'testProjectName-fullVersionString-image-montage.png'
    }

    def 'test createMontage'() {
        when:
        List<File> filesToMontage = imageMontageTask.getFilesToMontage(testMontageFilesDir)
        File montage = imageMontageTask.outputMontageFile()
        imageMontageTask.createMontage(montage, filesToMontage)

        then:
        montage.exists()
        println "Montage path: ${montage.absolutePath}"

        def image = ImageIO.read(montage)

        int size = filesToMontage.count { imageMontageTask.getImageFrom(it) != null }
//        def size = filesToMontage.size()
        int columns = Math.min(size, imageMontageTask.MAX_NUMBER_OF_TILES_IN_ROW)
        int rows = Math.ceil(size / columns)
        image.getWidth() == ImageMontageTask.TILE_PX_SIZE * columns
        image.getHeight() == ImageMontageTask.TILE_PX_SIZE * rows
    }

    def "test getFilesToMontage"() {
        expect:
        testMontageFilesDir.exists()

        when:
        def files = imageMontageTask.getFilesToMontage(testMontageFilesDir)
        def filenames = files.collect { it.absolutePath.split('/')[-1] }

        then:
        files.size() == 12
        filenames.sort() == ['1.bmp', '1.gif', '1.jpeg', '1.jpg', '1.png', '1.raw', '1.svg', '1.tif', '1.tiff', '1.webp', '2.png', '3.png']
    }

    def 'svg convertion'() {
        given:
        def svgFile = new File('src/test/resources/com/apphance/ameba/plugins/release/tasks/montageFiles/montageFilesSubdir/1.svg')
        def images = imageMontageTask.resizeImages([svgFile])

        expect:
        svgFile.exists()
        images.size() == 1
    }

    def 'compute width and height'() {
        expect:
        imageMontageTask.computeWidhtHeight(numberOfImages) == [width, height]

        where:
        numberOfImages | width | height
        1              | 1     | 1
        2              | 2     | 1
        10             | 10    | 1
        11             | 10    | 2
        99             | 10    | 10
        100            | 10    | 10
        101            | 10    | 11
    }

    @Unroll
    def '#file convertion'() {
        given:
        def dir = 'src/test/resources/com/apphance/ameba/plugins/release/tasks/montageFiles/montageFilesSubdir/'
        def source = new File(dir + '1.' + file)

        expect:
        source.exists()
        imageMontageTask.getImageFrom(source) != null

        where:
        file << ['tif', 'tiff', 'webp', 'jpg', 'jpeg', 'gif', 'png', 'raw', 'bmp', 'svg']
    }

    @Ignore('manual verification')
    def 'manually verify generated montage'() {
        when:
        List<File> filesToMontage = imageMontageTask.getFilesToMontage(testMontageFilesDir)
        filesToMontage.each {
            println(it.absolutePath)
        }
        File montage = imageMontageTask.outputMontageFile()
        imageMontageTask.createMontage(montage, filesToMontage)
        println montage.absolutePath

        then:
        montage.exists()
        show(montage)
    }

    @Ignore('This test should be run and verified manually')
    def 'add descrption'() {
        given:
        def tempFile = File.createTempFile('file-with-desc-', '.png')
        Files.copy(fileForDescTest, tempFile)

        when:
        imageMontageTask.addDescription(tempFile, "Test desc")

        then:
        tempFile.exists()
        show(tempFile)
    }

    private void show(File tempFile) {
        def img = new ImagePlus("", ImageIO.read(tempFile))
        img.show()
        sleep(5000)
    }
}
