package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.ProjectConfiguration
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

    ImageMontageTask(Project project, CommandExecutor executor) {
        this.project = project
        this.conf = getProjectConfiguration(project)
        this.releaseConf = retrieveProjectReleaseData(project)
        this.executor = executor
    }

    void imageMontage() {
        Collection<String> command = new LinkedList<String>()
        command << 'montage'
        project.rootDir.traverse([type: FILES, maxDepth: MAX_RECURSION_LEVEL]) { file ->
            if (isValid(project.rootDir, file)) {
                command << file
            }
        }
        def tempFile = File.createTempFile("image_montage_${conf.projectName}", '.png')
        command << tempFile.toString()
        executor.executeCommand(new Command(cmd: command, runDir: project.rootDir))
        def imageMontageFile = new File(releaseConf.targetDirectory, "${conf.projectName}-${conf.fullVersionString}-image-montage.png")
        imageMontageFile.parentFile.mkdirs()
        imageMontageFile.delete()
        String[] convertCommand = [
                '/opt/local/bin/convert',
                tempFile,
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
}
