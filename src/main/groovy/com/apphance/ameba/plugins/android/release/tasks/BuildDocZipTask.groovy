package com.apphance.ameba.plugins.android.release.tasks

import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.google.common.base.Preconditions
import com.google.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static org.gradle.api.logging.Logging.getLogger

class BuildDocZipTask extends DefaultTask {

    private l = getLogger(this.class)

    static String name = 'buildDocumentationZip'
    String description = 'Builds documentation .zip file'
    String group = AMEBA_RELEASE

    @Inject AndroidReleaseConfiguration releaseConf

    @TaskAction
    public void buildDocZip() {
        Preconditions.checkNotNull(releaseConf?.documentationZip?.location)

        File destZip = releaseConf.documentationZip.location
        destZip.mkdirs()
        destZip.delete()
        File javadocDir = project.file('build/docs/javadoc')
        project.ant.zip(destfile: destZip, basedir: javadocDir)
        l.debug("Zipped documentation written to: $destZip.absolutePath")
    }
}
