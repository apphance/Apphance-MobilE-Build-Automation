package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.plugins.release.AmebaArtifact
import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration
import org.gradle.api.Project

import static com.apphance.ameba.plugins.release.ProjectReleaseCategory.retrieveProjectReleaseData

class CopyGalleryFilesTask {

    private ProjectReleaseConfiguration releaseConf

    CopyGalleryFilesTask(Project project) {
        this.releaseConf = retrieveProjectReleaseData(project)
    }

    public void copy() {
        prepareGalleryArtifacts()
        releaseConf.galleryCss.location.parentFile.mkdirs()
        releaseConf.galleryJs.location.parentFile.mkdirs()
        releaseConf.galleryCss.location.setText(getClass().getResourceAsStream('swipegallery/_css/jquery.swipegallery.css').text, 'utf-8')
        releaseConf.galleryJs.location.setText(getClass().getResourceAsStream('swipegallery/_res/jquery.swipegallery.js').text, 'utf-8')
        releaseConf.galleryTrans.location.setText(getClass().getResourceAsStream('swipegallery/_res/trans.png').text, 'utf-8')
    }

    private prepareGalleryArtifacts() {
        releaseConf.galleryCss = new AmebaArtifact(
                name: 'CSS Gallery',
                url: new URL(releaseConf.versionedApplicationUrl, '_css/jquery.swipegallery.css'),
                location: new File(releaseConf.targetDirectory, '_css/jquery.swipegallery.css'))
        releaseConf.galleryJs = new AmebaArtifact(
                name: 'JS Gallery',
                url: new URL(releaseConf.versionedApplicationUrl, '_res/jquery.swipegallery.js'),
                location: new File(releaseConf.targetDirectory, '_res/jquery.swipegallery.js'))
        releaseConf.galleryTrans = new AmebaArtifact(
                name: 'JS Gallery',
                url: new URL(releaseConf.versionedApplicationUrl, '_res/trans.png'),
                location: new File(releaseConf.targetDirectory, '_res/trans.png'))
    }
}
