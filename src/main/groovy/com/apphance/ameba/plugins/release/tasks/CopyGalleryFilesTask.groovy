package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.configuration.release.ReleaseConfiguration
import com.apphance.ameba.plugins.release.AmebaArtifact
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION

class CopyGalleryFilesTask extends DefaultTask {

    static String NAME = 'copyGalleryFiles'
    String group = AMEBA_CONFIGURATION
    String description = 'Copy files required by swipe jquerymobile gallery'

    @Inject
    private ReleaseConfiguration releaseConf


    @TaskAction
    void copy() {
        prepareGalleryArtifacts()
        releaseConf.galleryCSS.location.parentFile.mkdirs()
        releaseConf.galleryJS.location.parentFile.mkdirs()
        releaseConf.galleryCSS.location.setText(getClass().getResourceAsStream('swipegallery/_css/jquery.swipegallery.css').text, 'utf-8')
        releaseConf.galleryJS.location.setText(getClass().getResourceAsStream('swipegallery/_res/jquery.swipegallery.js').text, 'utf-8')
        releaseConf.galleryTrans.location.setText(getClass().getResourceAsStream('swipegallery/_res/trans.png').text, 'utf-8')
    }

    private prepareGalleryArtifacts() {
        releaseConf.galleryCSS = new AmebaArtifact(
                name: 'CSS Gallery',
                url: new URL(releaseConf.versionedApplicationUrl, '_css/jquery.swipegallery.css'),
                location: new File(releaseConf.targetDirectory, '_css/jquery.swipegallery.css'))
        releaseConf.galleryJS = new AmebaArtifact(
                name: 'JS Gallery',
                url: new URL(releaseConf.versionedApplicationUrl, '_res/jquery.swipegallery.js'),
                location: new File(releaseConf.targetDirectory, '_res/jquery.swipegallery.js'))
        releaseConf.galleryTrans = new AmebaArtifact(
                name: 'JS Gallery',
                url: new URL(releaseConf.versionedApplicationUrl, '_res/trans.png'),
                location: new File(releaseConf.targetDirectory, '_res/trans.png'))
    }
}
