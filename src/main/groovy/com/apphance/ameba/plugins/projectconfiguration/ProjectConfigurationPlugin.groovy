package com.apphance.ameba.plugins.projectconfiguration;


import org.gradle.api.GradleException;
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AmebaArtifact
import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory


/**
 * Plugin for Mercurial implementation of VCS system
 *
 */
class ProjectConfigurationPlugin implements Plugin<Project> {

    static Logger logger = Logging.getLogger(ProjectConfigurationPlugin.class)

    static public final String DESCRIPTION ="""
<div>
<div>This is the base plugin which should be applied by any project.</div>
<div><br></div>
<div>The plugin should be applied before any other plugin. It reads basic project configuration and loads shared
configuration for all other plugins. Other plugins use this plugin's generated task.</div>
<div>This plugin provides also setup-related tasks. The tasks allow to generate new configuration,
verify existing configurationa and show the configuration to the user.
It also adds several utility tasks that can be used across all types of projects.&nbsp;</div>
</div>
"""

    static final String PROJECT_NAME_PROPERTY = 'project.name'

    ProjectHelper projectHelper
    ProjectConfiguration conf

    void apply(Project project) {
        projectHelper = new ProjectHelper()
        prepareRepositories(project)
        readProjectConfigurationTask(project)
        project.task('prepareSetup', type: PrepareSetupTask.class)
        project.task('verifySetup', type: VerifySetupTask.class)
        project.task('showSetup', type: ShowSetupTask.class)
        project.task('checkTests', type: CheckTestsTask.class)
        project.task('showConventions', type: ShowConventionsTask.class)
        prepareShowConventionRule(project)
        prepareCleanConfigurationTask(project)
        prepareCopyGalleryFilesTask(project)
        project.prepareSetup.prepareSetupOperations << new PrepareBaseSetupOperation()
        project.verifySetup.verifySetupOperations << new VerifyBaseSetupOperation()
        project.showSetup.showSetupOperations << new ShowBaseSetupOperation()
    }

    private void prepareShowConventionRule(Project project) {
        project.tasks.addRule("Pattern: showConvention<ConventionName>: Shows current convention values for convention specified by name") { String taskName ->
            if (taskName.startsWith("showConvention")) {
                project.task(taskName) << {
                    String pluginName = taskName.substring('showConvention'.length()).replaceAll('^.') { it.toLowerCase() }
                    pluginConventionObject = project.convention.plugins[pluginName]
                    if (pluginConventionObject == null) {
                        throw new GradleException("There is no convention with ${pluginName} name")
                    }
                    StringBuilder sb = new StringBuilder()
                    new ShowConventionHelper().getConventionObject(sb, pluginName, pluginConventionObject)
                    println sb
                }
            }
        }
    }

    void prepareRepositories(Project project) {
        project.repositories.mavenCentral()
    }

    def void readProjectConfigurationTask(Project project) {
        def task = project.task('readProjectConfiguration')
        task.description = "Reads project's configuration and sets it up in projectConfiguration property of project"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
        task << {
            use (PropertyCategory) {
                this.conf = project.getProjectConfiguration()
                // NOTE! conf.versionString and conf.versionCode need to
                // be read before project configuration task -> task reading the version
                // should be injected here
                project.retrieveBasicProjectData()
                prepareGeneratedDirectories(project)
            }
        }
    }

    private prepareGeneratedDirectories(Project project) {
        conf.otaDirectory = new File(project.rootDir,"ota/")
        conf.tmpDirectory = new File(project.rootDir,"tmp/")
    }

    def void prepareCleanConfigurationTask(Project project) {
        def task = project.task('cleanConfiguration')
        task.description = "Cleans configuration before each build"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
        task << {
            conf.buildDirectory.deleteDir()
            conf.tmpDirectory.deleteDir()
            conf.logDirectory.deleteDir()
            conf.buildDirectory.mkdirs()
            conf.logDirectory.mkdirs()
            conf.tmpDirectory.mkdirs()
        }
        task.dependsOn(project.readProjectConfiguration)
    }

    private prepareGalleryArtifacts() {
        conf.galleryCss = new AmebaArtifact(
                name : "CSS Gallery",
                url : new URL(conf.versionedApplicationUrl, "_css/jquery.swipegallery.css"),
                location : new File(conf.targetDirectory, "_css/jquery.swipegallery.css"))
        conf.galleryJs = new AmebaArtifact(
                name : "JS Gallery",
                url : new URL(conf.versionedApplicationUrl, "_res/jquery.swipegallery.js"),
                location : new File(conf.targetDirectory, "_res/jquery.swipegallery.js"))
        conf.galleryTrans = new AmebaArtifact(
                name : "JS Gallery",
                url : new URL(conf.versionedApplicationUrl, "_res/trans.png"),
                location : new File(conf.targetDirectory, "_res/trans.png"))
    }

    def void prepareCopyGalleryFilesTask(Project project) {
        def task = project.task('copyGalleryFiles')
        task.group = AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
        task.description = "Copy files required by swipe jquerymobile gallery"
        task << {
            prepareGalleryArtifacts()
            conf.galleryCss.location.parentFile.mkdirs()
            conf.galleryJs.location.parentFile.mkdirs()
            conf.galleryCss.location.setText(this.class.getResourceAsStream("swipegallery/_css/jquery.swipegallery.css").text,"utf-8")
            conf.galleryJs.location.setText(this.class.getResourceAsStream("swipegallery/_res/jquery.swipegallery.js").text,"utf-8")
            conf.galleryTrans.location.setText(this.class.getResourceAsStream("swipegallery/_res/trans.png").text,"utf-8")
        }
        task.dependsOn(project.readProjectConfiguration)
    }
}