package com.apphance.flow.plugins

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.detection.project.ProjectType
import com.apphance.flow.detection.project.ProjectTypeDetector
import com.apphance.flow.docs.DocPluginMasterMixin
import com.apphance.flow.plugins.android.analysis.AndroidAnalysisPlugin
import com.apphance.flow.plugins.android.apphance.AndroidApphancePlugin
import com.apphance.flow.plugins.android.buildplugin.AndroidPlugin
import com.apphance.flow.plugins.android.release.AndroidReleasePlugin
import com.apphance.flow.plugins.android.test.AndroidTestPlugin
import com.apphance.flow.plugins.ios.apphance.IOSApphancePlugin
import com.apphance.flow.plugins.ios.buildplugin.IOSPlugin
import com.apphance.flow.plugins.ios.release.IOSReleasePlugin
import com.apphance.flow.plugins.ios.test.IOSTestPlugin
import com.apphance.flow.plugins.project.ProjectPlugin
import com.apphance.flow.plugins.release.ReleasePlugin
import com.google.inject.Injector
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

import javax.inject.Inject

import static com.apphance.flow.configuration.reader.GradlePropertiesPersister.FLOW_PROP_FILENAME
import static java.util.ResourceBundle.getBundle
import static org.gradle.api.logging.Logging.getLogger

@Mixin(DocPluginMasterMixin)
class PluginMaster {

    def logger = getLogger(getClass())
    def docBundle = getBundle('doc')

    @Inject ProjectTypeDetector projectTypeDetector
    @Inject Injector injector
    @Inject Map<Integer, AbstractConfiguration> configurations

    final static PLUGINS = [
            COMMON: [
                    ProjectPlugin,
                    ReleasePlugin,
            ],

            IOS: [
                    IOSPlugin,
                    IOSReleasePlugin,
                    IOSApphancePlugin,
                    IOSTestPlugin,
            ],

            ANDROID: [
                    AndroidPlugin,
                    AndroidAnalysisPlugin,
                    AndroidApphancePlugin,
                    AndroidReleasePlugin,
                    AndroidTestPlugin,
            ],
    ]

    void enhanceProject(Project project) {
        ProjectType projectType = projectTypeDetector.detectProjectType(project.rootDir)

        Map<Integer, Map> plugins = [:]

        def docModeEnabled = docModeEnabled(project)
        def docFile = docModeEnabled ? docFile(project) : null
        def idx = 0

        if (docModeEnabled)
            logger.lifecycle "'docMode' enabled - generating documentation"

        def installPlugin = {
            Plugin<Project> plugin = (Plugin<Project>) injector.getInstance(it)

            Set<Task> tasksBefore = null
            if (docModeEnabled)
                tasksBefore = project.tasks.findAll()

            plugin.apply(project)

            if (docModeEnabled)
                plugins[idx++] = [
                        plugin: plugin,
                        tasks: (project.tasks.findAll() - tasksBefore).findAll { it.enabled }
                ]

            project.plugins.add(plugin)
        }

        PLUGINS['COMMON'].each installPlugin

        if (project.file(FLOW_PROP_FILENAME).exists())
            PLUGINS[projectType.name()].each installPlugin

        if (docModeEnabled && docFile?.exists())
            saveDoc(plugins, configurations, docFile)
    }
}
