package com.apphance.flow.plugins.release.tasks

import com.apphance.flow.TestUtils
import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.command.CommandLogFilesGenerator
import com.apphance.flow.util.FlowUtils
import com.google.common.io.Files
import ij.ImagePlus
import org.gradle.api.Project
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import javax.imageio.ImageIO

import static com.apphance.flow.configuration.ProjectConfiguration.TMP_DIR
import static com.apphance.flow.configuration.release.ReleaseConfiguration.OTA_DIR
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.STD
import static java.io.File.createTempFile

@Mixin([TestUtils, FlowUtils])
class ImageMontageTaskSpec extends Specification {

    def imageMontageTask = create ImageMontageTask

    private File testMontageFilesDir = new File('src/test/resources/com/apphance/flow/plugins/release/tasks/montageFiles')
    def fileForDescTest = new File('src/test/resources/com/apphance/flow/plugins/release/tasks/Blank.jpg')

    def setup() {
        def project = GroovyStub(Project)
        def conf = GroovySpy(AndroidConfiguration)
        def releaseConf = GroovyStub(AndroidReleaseConfiguration)
        def logFileGenerator = Stub(CommandLogFilesGenerator)

        def testDir = Files.createTempDir()
        testDir.deleteOnExit()
        releaseConf.getReleaseDir() >> testDir
        releaseConf.otaDir >> new File(OTA_DIR)
        conf.getProjectName() >> new StringProperty(value: 'testProjectName')
        conf.getVersionString() >> 'vs'
        conf.getVersionCode() >> 'vc'
        conf.project = GroovyStub(Project) {
            file(TMP_DIR) >> new File(TMP_DIR)
        }
        logFileGenerator.commandLogFiles() >> [(ERR): createTempFile('err', 'log'), (STD): createTempFile('std', 'log')]
        imageMontageTask.project >> project
        imageMontageTask.conf = conf
        imageMontageTask.releaseConf = releaseConf
    }

    def "test outputMontageFile"() {
        when:
        def file = imageMontageTask.outputMontageFile()

        then:
        !file.exists()
        file.name == 'testProjectName-vs_vc-image-montage.png'
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
        files.size() == 9
        filenames.sort() == ['1.bmp', '1.gif', '1.jpeg', '1.jpg', '1.png', '1.raw', '1.svg', '2.png', '3.png']
    }

    @Ignore('batik:batik-transcoder:1.6-1 causes xercesImpl version conflict')
    def 'svg conversion'() {
        given:
        def svgFile = new File('src/test/resources/com/apphance/flow/plugins/release/tasks/montageFiles/montageFilesSubdir/1.svg')
        def images = imageMontageTask.resizeImages([svgFile])

        expect:
        svgFile.exists()
        images.size() == 1
    }

    def 'compute width and height'() {
        expect:
        imageMontageTask.computeWidthHeight(numberOfImages) == [width, height]

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
    def '#file conversion'() {
        given:
        def dir = 'src/test/resources/com/apphance/flow/plugins/release/tasks/montageFiles/montageFilesSubdir/'
        def source = new File(dir + '1.' + file)

        expect:
        source.exists()
        imageMontageTask.getImageFrom(source) != null

        where:
        file << ['jpg', 'jpeg', 'gif', 'png', 'raw', 'bmp']
    }

    def 'create montage when no images'() {
        given:
        def output = tempFile

        when:
        imageMontageTask.createMontage(output, [])

        then:
        ImageIO.read(output)
    }

    def 'test read invalid png'() {
        given:
        def ihdrBadLength = new File('src/test/resources/com/apphance/flow/plugins/release/tasks/bad_length_ihdr.png')

        expect:
        imageMontageTask.getImageFrom(ihdrBadLength) == null
    }

    @Ignore('This test should be run and verified manually')
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
    def 'manually verify adding description'() {
        given:
        def tempFile = createTempFile('file-with-desc-', '.png')
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
