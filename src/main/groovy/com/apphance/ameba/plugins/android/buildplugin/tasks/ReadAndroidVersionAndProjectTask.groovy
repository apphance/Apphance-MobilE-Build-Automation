package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.plugins.projectconfiguration.ProjectConfiguration
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.plugins.android.AndroidBuildXmlHelper
import com.apphance.ameba.plugins.android.AndroidManifestHelper
import org.gradle.api.Project

import static com.apphance.ameba.PropertyCategory.getProjectConfiguration
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.PROJECT_NAME_PROPERTY
import static org.gradle.api.logging.Logging.getLogger

//TODO test + refactor
class ReadAndroidVersionAndProjectTask {

    private l = getLogger(getClass())

    private Project project
    private ProjectConfiguration conf
    private AndroidManifestHelper manifestHelper = new AndroidManifestHelper()

    ReadAndroidVersionAndProjectTask(Project project) {
        this.project = project
        this.conf = getProjectConfiguration(project)
    }

    void readAndroidVersionAndProjectTask() {
        conf.updateVersionDetails(manifestHelper.readVersion(project.rootDir))
        use(PropertyCategory) {
            if (!project.isPropertyOrEnvironmentVariableDefined('version.string')) {
                l.lifecycle("Version string is updated to SNAPSHOT because it is not release build")
                conf.versionString = conf.versionString + "-SNAPSHOT"
            } else {
                conf.versionString = project.getPropertyOrEnvironmentVariableDefined('version.string')
                l.lifecycle("Version string is not updated to SNAPSHOT because it is release build. Given version is ${conf.versionString}")
            }
        }
        AndroidBuildXmlHelper buildXmlHelper = new AndroidBuildXmlHelper()
        project.ext[PROJECT_NAME_PROPERTY] = buildXmlHelper.projectName(project.rootDir)
    }
}
