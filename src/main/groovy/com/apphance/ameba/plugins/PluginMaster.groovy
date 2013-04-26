package com.apphance.ameba.plugins

import com.apphance.ameba.detection.ProjectType
import com.apphance.ameba.detection.ProjectTypeDetector
import com.apphance.ameba.plugins.android.analysis.AndroidAnalysisPlugin
import com.apphance.ameba.plugins.android.apphance.AndroidApphancePlugin
import com.apphance.ameba.plugins.android.buildplugin.AndroidPlugin
import com.apphance.ameba.plugins.android.jarlibrary.AndroidJarLibraryPlugin
import com.apphance.ameba.plugins.android.release.AndroidReleasePlugin
import com.apphance.ameba.plugins.android.test.AndroidTestPlugin
import com.apphance.ameba.plugins.ios.apphance.IOSApphancePlugin
import com.apphance.ameba.plugins.ios.buildplugin.IOSPlugin
import com.apphance.ameba.plugins.ios.framework.IOSFrameworkPlugin
import com.apphance.ameba.plugins.ios.ocunit.IOSUnitTestPlugin
import com.apphance.ameba.plugins.ios.release.IOSReleasePlugin
import com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin
import com.apphance.ameba.plugins.release.ProjectReleasePlugin
import com.google.inject.Injector
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging

import javax.inject.Inject

class PluginMaster {

    def log = Logging.getLogger(getClass())

    @Inject ProjectTypeDetector projectTypeDetector

    @Inject Injector injector

    static plugins = [
            COMMON: [
                    ProjectConfigurationPlugin,
            ],

            IOS: [
                    IOSPlugin,
                    ProjectReleasePlugin,
                    IOSFrameworkPlugin,
                    IOSReleasePlugin,
                    IOSApphancePlugin,
                    IOSUnitTestPlugin,
            ],

            ANDROID: [
                    AndroidPlugin,
                    ProjectReleasePlugin,
                    AndroidAnalysisPlugin,
                    AndroidApphancePlugin,
                    AndroidJarLibraryPlugin,
                    AndroidReleasePlugin,
                    AndroidTestPlugin,
            ],
    ]

    void enhanceProject(Project project) {
        ProjectType projectType = projectTypeDetector.detectProjectType(project.rootDir)

        def installPlugin = {
            log.info("Applying plugin $it")

            Plugin<Project> plugin = (Plugin<Project>) injector.getInstance(it)

            plugin.apply(project)
            project.plugins.add(plugin)
        }

        plugins['COMMON'].each installPlugin

        if (project.file('ameba.properties').exists()) {
            plugins[projectType.name()].each installPlugin
        }
    }
}
