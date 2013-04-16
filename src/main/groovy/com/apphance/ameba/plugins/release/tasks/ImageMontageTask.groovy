package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.configuration.ReleaseConfiguration
import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.release.AmebaArtifact
import org.gradle.api.DefaultTask

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static com.apphance.ameba.util.file.FileManager.MAX_RECURSION_LEVEL
import static groovy.io.FileType.FILES
import static org.gradle.api.logging.Logging.getLogger

@Mixin(ImageNameFilter)
class ImageMontageTask extends DefaultTask {

    private l = getLogger(getClass())

    static String NAME = 'prepareImageMontage'
    String group = AMEBA_RELEASE
    String description = 'Builds montage of images found in the project'

    @Inject
    private CommandExecutor executor
    @Inject
    private ProjectConfiguration conf
    @Inject
    private ReleaseConfiguration releaseConf

    void imageMontage() {
        Collection<String> command = new LinkedList<String>()
        command << 'montage'
        project.rootDir.traverse([type: FILES, maxDepth: MAX_RECURSION_LEVEL]) { file ->
            if (isValid(project.rootDir, file)) {
                command << file
            }
        }
        def tempFile = File.createTempFile("image_montage_${conf.projectName.value}", '.png')
        command << tempFile.toString()
        executor.executeCommand(new Command(cmd: command, runDir: project.rootDir))
        def imageMontageFile = new File(releaseConf.targetDirectory, "${conf.projectName.value}-${conf.fullVersionString}-image-montage.png")
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
                "gravity southwest fill black text 0,12 '${conf.projectName.value} Version: ${conf.fullVersionString} Generated: ${releaseConf.buildDate}'",
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
