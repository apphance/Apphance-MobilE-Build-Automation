package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.plugins.release.AmebaArtifact
import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration
import org.gradle.api.Project

import static com.apphance.ameba.PropertyCategory.getProjectConfiguration
import static com.apphance.ameba.plugins.release.ProjectReleaseCategory.retrieveProjectReleaseData
import static com.apphance.ameba.plugins.release.ProjectReleaseProperty.*

class PrepareForReleaseTask {

    private Project project
    private ProjectConfiguration conf
    private ProjectReleaseConfiguration releaseConf

    PrepareForReleaseTask(Project project) {
        this.project = project
        this.conf = getProjectConfiguration(project)
        this.releaseConf = retrieveProjectReleaseData(project)
    }

    public void prepare() {
        prepareSourcesAndDocumentationArtifacts()
        prepareMailArtifacts()
    }

    private prepareSourcesAndDocumentationArtifacts() {
        def sourceZipName = conf.projectVersionedName + "-src.zip"
        releaseConf.sourcesZip = new AmebaArtifact(
                name: conf.projectName + "-src",
                url: null, // we do not publish
                location: new File(conf.tmpDirectory, sourceZipName))
        def documentationZipName = conf.projectVersionedName + "-doc.zip"
        releaseConf.documentationZip = new AmebaArtifact(
                name: conf.projectName + "-doc",
                url: null,
                location: new File(conf.tmpDirectory, documentationZipName))
        releaseConf.targetDirectory.mkdirs()
    }

    private prepareMailArtifacts() {
        releaseConf.mailMessageFile = new AmebaArtifact(
                name: "Mail message file",
                url: new URL(releaseConf.versionedApplicationUrl, "message_file.html"),
                location: new File(releaseConf.targetDirectory, "message_file.html"))
        use(PropertyCategory) {
            releaseConf.releaseMailFrom = project.readExpectedProperty(RELEASE_MAIL_FROM)
            releaseConf.releaseMailTo = project.readExpectedProperty(RELEASE_MAIL_TO)
            releaseConf.releaseMailFlags = []
            String flags = project.readProperty(RELEASE_MAIL_FLAGS)
            if (flags != null) {
                releaseConf.releaseMailFlags = flags.tokenize(",").collect { it.trim() }
            }
        }
    }
}

