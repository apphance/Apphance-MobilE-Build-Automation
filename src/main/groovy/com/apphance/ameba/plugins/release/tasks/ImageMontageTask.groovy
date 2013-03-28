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

    private l = getLogger(this.class)
    Project project
    ProjectConfiguration conf
    ProjectReleaseConfiguration releaseConf
    CommandExecutor executor

    ImageMontageTask() {
    }

    ImageMontageTask(Project project, CommandExecutor executor) {
        this.project = project
        this.conf = getProjectConfiguration(project)
        this.releaseConf = retrieveProjectReleaseData(project)
        this.executor = executor
    }

    AmebaArtifact imageMontage() {
        def filesToMontage = getFilesToMontage(project.rootDir)
        File imageMontageFile = createMontage(filesToMontage)
        addDescription(imageMontageFile, "${conf.projectName} Version: ${conf.fullVersionString} Generated: ${releaseConf.buildDate}")

        def imageMontageFileArtifact = new AmebaArtifact(
                name: "Image Montage",
                url: new URL(releaseConf.versionedApplicationUrl, "${imageMontageFile.name}"),
                location: imageMontageFile)
        releaseConf.imageMontageFile = imageMontageFileArtifact
    }

    void addDescription(File image, String description) {
        String[] convertCommand = [
                '/opt/local/bin/convert',
                image,
                '-font',
                'helvetica',
                '-pointsize',
                '36',
                '-draw',
                "gravity southwest fill black text 0,12 '${description}'",
                image
        ]
        executor.executeCommand(new Command(cmd: convertCommand, runDir: project.rootDir))
    }

    @groovy.transform.PackageScope
    File outputMontageFile() {
        def imageMontageFile = new File(releaseConf.targetDirectory, "${conf.projectName}-${conf.fullVersionString}-image-montage.png")
        imageMontageFile.parentFile.mkdirs()
        imageMontageFile.delete()
        imageMontageFile
    }

    File createMontage(List<File> sourceFiles) {
        File imageMontageFile = outputMontageFile()
        def command = [] << 'montage' << sourceFiles.collect { it.toString() } << imageMontageFile.toString()
        executor.executeCommand(new Command(cmd: command.flatten(), runDir: project.rootDir))

        imageMontageFile
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
