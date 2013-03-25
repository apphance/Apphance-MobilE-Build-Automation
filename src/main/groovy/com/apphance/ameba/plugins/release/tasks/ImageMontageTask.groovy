package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.plugins.projectconfiguration.ProjectConfiguration
import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.release.AmebaArtifact
import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration
import org.gradle.api.Project

import static com.apphance.ameba.PropertyCategory.getProjectConfiguration
import static com.apphance.ameba.plugins.release.ProjectReleaseCategory.retrieveProjectReleaseData
import static com.apphance.ameba.util.file.FileManager.MAX_RECURSION_LEVEL
import static groovy.io.FileType.FILES
import static org.gradle.api.logging.Logging.getLogger

@Mixin(ImageNameFilter)
class ImageMontageTask {

    private l = getLogger(getClass())
    private Project project
    private ProjectConfiguration conf
    private ProjectReleaseConfiguration releaseConf

    private CommandExecutor executor

    ImageMontageTask() {
    }

    ImageMontageTask(Project project, CommandExecutor executor) {
        this.project = project
        this.conf = getProjectConfiguration(project)
        this.releaseConf = retrieveProjectReleaseData(project)
        this.executor = executor
    }

    void imageMontage() {
        def imageMontageFile = new File(releaseConf.targetDirectory, "${conf.projectName}-${conf.fullVersionString}-image-montage.png")
        imageMontageFile.parentFile.mkdirs()
        imageMontageFile.delete()

        createMontage(imageMontageFile, getFilesToMontage(project.rootDir))
        String[] convertCommand = [
                '/opt/local/bin/convert',
                imageMontageFile,
                '-font',
                'helvetica',
                '-pointsize',
                '36',
                '-draw',
                "gravity southwest fill black text 0,12 '${conf.projectName} Version: ${conf.fullVersionString} Generated: ${releaseConf.buildDate}'",
                imageMontageFile
        ]
        try {
            executor.executeCommand(new Command(cmd: convertCommand, runDir: project.rootDir))
            def imageMontageFileArtifact = new AmebaArtifact(
                    name: "Image Montage",
                    url: new URL(releaseConf.versionedApplicationUrl, "${imageMontageFile.name}"),
                    location: imageMontageFile)
            releaseConf.imageMontageFile = imageMontageFileArtifact
        } catch (Exception e) {
            l.error("The convert binary execution failed: skipping image montage preparation. Add convert (ImageMagick) binary to the path to get image montage.")
        }
    }

    void createMontage(File outputFile, List<File> sourceFiles) {
        def command = []
        command << 'montage'
        sourceFiles.each { command << it.toString() }
        command << outputFile.toString()

        executor.executeCommand(new Command(cmd: command, runDir: project.rootDir))
    }

    List<File> getFilesToMontage(File rootDir) {
        List<File> filesToMontage = []

        rootDir.traverse([type: FILES, maxDepth: MAX_RECURSION_LEVEL]) { File file ->
            if (isValid(rootDir, file)) {
                filesToMontage << file
            }
        }
        filesToMontage
    }
}
