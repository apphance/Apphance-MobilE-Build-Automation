package com.apphance.ameba.plugins.release

import java.text.SimpleDateFormat
import java.util.ResourceBundle;

import org.gradle.api.Project

import com.apphance.ameba.ProjectConfiguration;
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.plugins.projectconfiguration.BaseProperty

class ProjectReleaseCategory {

    public static final String PROJECT_RELEASE_CONFIGURATION_KEY = 'project.release.configuration'

    public static ProjectReleaseConfiguration getProjectReleaseConfiguration(Project project){
        if (!project.hasProperty(PROJECT_RELEASE_CONFIGURATION_KEY)) {
            project[PROJECT_RELEASE_CONFIGURATION_KEY] = new ProjectReleaseConfiguration()
        }
        return project[PROJECT_RELEASE_CONFIGURATION_KEY]
    }

    public static void retrieveLocale(Project project) {
        use(PropertyCategory) {
            ProjectReleaseConfiguration releaseConf = getProjectReleaseConfiguration(project)
            String language = project.readProperty(BaseProperty.PROJECT_LANGUAGE)
            String country = project.readProperty(BaseProperty.PROJECT_COUNTRY)
            if (language == null) {
                releaseConf.locale = Locale.getDefault()
            } else {
                if (country == null) {
                    releaseConf.locale = new Locale(language)
                } else {
                    releaseConf.locale = new Locale(language,country)
                }
            }
            releaseConf.buildDate = new SimpleDateFormat("dd-MM-yyyy HH:mm zzz", releaseConf.locale).format(new Date())
        }
    }

    public static ProjectReleaseConfiguration retrieveProjectReleaseData(Project project) {
        use (PropertyCategory) {
            ProjectReleaseConfiguration releaseConf = getProjectReleaseConfiguration(project)
            releaseConf.projectConfiguration = project.getProjectConfiguration()
            releaseConf.projectDirectoryName = project.readProperty(BaseProperty.PROJECT_DIRECTORY)
            def url = project.readProperty(BaseProperty.PROJECT_URL)
            println url
            releaseConf.baseUrl = url == null ? null : new URL(url)
            def iconFile = project.readProperty(BaseProperty.PROJECT_ICON_FILE)
            releaseConf.iconFile = iconFile == null ? null : project.file(iconFile)
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
            def notes =  System.getenv('RELEASE_NOTES')
            if (notes == null || notes == "") {
                return null
            }
            project['release.notes'] = notes
            return notes
        }
    }

    public static void fillMailSubject(Project project, ResourceBundle resourceBundle) {
        ProjectConfiguration conf = PropertyCategory.getProjectConfiguration(project)
        ProjectReleaseConfiguration releaseConf = getProjectReleaseConfiguration(project)
        String subject = resourceBundle.getString('Subject')
        releaseConf.releaseMailSubject = Eval.me("conf",conf,/"$subject"/)
    }
}
