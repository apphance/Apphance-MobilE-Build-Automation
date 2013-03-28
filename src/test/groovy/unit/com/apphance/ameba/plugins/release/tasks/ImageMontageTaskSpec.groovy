package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.executor.command.CommandLogFilesGenerator
import com.apphance.ameba.executor.linker.FileLinker
import com.apphance.ameba.executor.linker.SimpleFileLinker
import com.apphance.ameba.plugins.projectconfiguration.ProjectConfiguration
import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration
import com.asprise.util.ocr.OCR
import com.google.common.io.Files
import org.gradle.api.Project
import spock.lang.Ignore
import spock.lang.Specification

import javax.imageio.ImageIO

import static com.apphance.ameba.executor.command.CommandLogFilesGenerator.LogFile.STD
import static com.apphance.ameba.executor.command.CommandLogFilesGenerator.LogFile.ERR
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
        File montage = imageMontageTask.createMontage(filesToMontage)

        then:
        montage.exists()
        def image = ImageIO.read(montage)

        image.getWidth() == 256
        image.getHeight() == 252
    }

    def "test getFilesToMontage"() {
        expect:
        testMontageFilesDir.exists()

        when:
        def files = imageMontageTask.getFilesToMontage(testMontageFilesDir)
        def filenames = files.collect { it.absolutePath.split('/')[-1] }

        then:
        files.size() == 4
        filenames.sort() == ['1.jpeg', '1.svg', '2.png', '3.png']
    }

    @Ignore('This test should be run and verified manually')
    def 'add descrption'() {
        given:
        def tempFile = File.createTempFile('file-with-desc', '.png')
        //tempFile.deleteOnExit()
        Files.copy(fileForDescTest, tempFile)

        when:
        imageMontageTask.addDescription(tempFile, "Test desc")

        then:
        println "open ${tempFile.absolutePath}"
    }
}
