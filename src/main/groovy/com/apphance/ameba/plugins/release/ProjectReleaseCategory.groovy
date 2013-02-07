package com.apphance.ameba.plugins.release

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.PropertyCategory
import org.gradle.api.Project

import java.text.SimpleDateFormat

/**
 *  Category used to get release-specific methods.
 *
 */
public class ProjectReleaseCategory {

    public static final String PROJECT_RELEASE_CONFIGURATION_KEY = 'project.release.configuration'

    public static ProjectReleaseConfiguration getProjectReleaseConfiguration(Project project) {
        if (!project.ext.has(PROJECT_RELEASE_CONFIGURATION_KEY)) {
            project.ext.set(PROJECT_RELEASE_CONFIGURATION_KEY, new ProjectReleaseConfiguration())
        }
        return project.ext.get(PROJECT_RELEASE_CONFIGURATION_KEY)
    }

    public static void retrieveLocale(Project project) {
        use(PropertyCategory) {
            ProjectReleaseConfiguration releaseConf = getProjectReleaseConfiguration(project)
            String language = project.readProperty(ProjectReleaseProperty.RELEASE_PROJECT_LANGUAGE)
            String country = project.readProperty(ProjectReleaseProperty.RELEASE_PROJECT_COUNTRY)
            if (language == null) {
                releaseConf.locale = Locale.getDefault()
            } else {
                if (country == null) {
                    releaseConf.locale = new Locale(language)
                } else {
                    releaseConf.locale = new Locale(language, country)
                }
            }
            releaseConf.buildDate = new SimpleDateFormat("dd-MM-yyyy HH:mm zzz", releaseConf.locale).format(new Date())
        }
    }

    public static ProjectReleaseConfiguration retrieveProjectReleaseData(Project project) {
        use(PropertyCategory) {
            ProjectReleaseConfiguration releaseConf = getProjectReleaseConfiguration(project)
            releaseConf.projectConfiguration = project.getProjectConfiguration()
            String urlString = project.readProperty(ProjectReleaseProperty.RELEASE_PROJECT_URL)
            if (urlString != null) {
                def baseUrl, directory
                (baseUrl, directory) = splitUrl(urlString)
                releaseConf.baseUrl = baseUrl
                releaseConf.projectDirectoryName = directory
            } else {
                // migration from old version TODO: remove me when not needed
                if (project.readProperty('project.url.base') != null) {
                    releaseConf.baseUrl = new URL(project.readProperty('project.url.base'))
                    releaseConf.projectDirectoryName = project.readProperty('project.directory.name')
                    if (releaseConf.baseUrl != null && releaseConf.projectDirectoryName != null) {
                        project.ext[ProjectReleaseProperty.RELEASE_PROJECT_URL.propertyName] =
                            new URL(releaseConf.baseUrl, releaseConf.projectDirectoryName)
                    }
                }
            }
            String iconFileName = project.readProperty(ProjectReleaseProperty.RELEASE_PROJECT_ICON_FILE)
            releaseConf.iconFile = (iconFileName == null || iconFileName.empty) ? null : project.file(iconFileName)
            releaseConf.otaDirectory = project.file('ota')
            retrieveLocale(project)
            releaseConf.releaseNotes = readReleaseNotes(project)?.tokenize(",")
            return releaseConf
        }
    }

    public static String readReleaseNotes(Project project) {
        if (project.hasProperty('release.notes')) {
            return project['release.notes']
        } else {
            def notes = System.getenv('RELEASE_NOTES')
            if (notes == null || notes == "") {
                return null
            }
            project.ext['release.notes'] = notes
            return notes
        }
    }

    public static void fillMailSubject(Project project, ResourceBundle resourceBundle) {
        ProjectConfiguration conf = PropertyCategory.getProjectConfiguration(project)
        ProjectReleaseConfiguration releaseConf = getProjectReleaseConfiguration(project)
        String subject = resourceBundle.getString('Subject')
        releaseConf.releaseMailSubject = Eval.me("conf", conf, /"$subject"/)
    }

    public static splitUrl(String urlString) {
        def url = new URL(urlString)
        def path = url.path
        def splitPath = path.split('/')
        def lastElement = splitPath[-1]
        def baseUrl = new URL(url.protocol, url.host, url.port, (splitPath[0..-2]).join('/') + '/')
        return [baseUrl, lastElement]
    }
}
