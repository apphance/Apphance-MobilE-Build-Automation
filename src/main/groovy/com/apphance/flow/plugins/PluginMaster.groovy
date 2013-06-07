package com.apphance.flow.plugins

import com.apphance.flow.detection.ProjectType
import com.apphance.flow.detection.ProjectTypeDetector
import com.apphance.flow.plugins.android.analysis.AndroidAnalysisPlugin
import com.apphance.flow.plugins.android.apphance.AndroidApphancePlugin
import com.apphance.flow.plugins.android.buildplugin.AndroidPlugin
import com.apphance.flow.plugins.android.jarlibrary.AndroidJarLibraryPlugin
import com.apphance.flow.plugins.android.release.AndroidReleasePlugin
import com.apphance.flow.plugins.android.test.AndroidTestPlugin
import com.apphance.flow.plugins.ios.apphance.IOSApphancePlugin
import com.apphance.flow.plugins.ios.buildplugin.IOSPlugin
import com.apphance.flow.plugins.ios.framework.IOSFrameworkPlugin
import com.apphance.flow.plugins.ios.ocunit.IOSUnitTestPlugin
import com.apphance.flow.plugins.ios.release.IOSReleasePlugin
import com.apphance.flow.plugins.project.ProjectPlugin
import com.apphance.flow.plugins.release.ProjectReleasePlugin
import com.google.inject.Injector
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging

import javax.inject.Inject

import static com.apphance.flow.configuration.reader.GradlePropertiesPersister.FLOW_PROP_FILENAME

class PluginMaster {

    def log = Logging.getLogger(getClass())

    @Inject ProjectTypeDetector projectTypeDetector
    @Inject Injector injector

    final static PLUGINS = [
            COMMON: [
                    ProjectPlugin,
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

        PLUGINS['COMMON'].each installPlugin

        if (project.file(FLOW_PROP_FILENAME).exists()) {
            PLUGINS[projectType.name()].each installPlugin
        }
    }
}
