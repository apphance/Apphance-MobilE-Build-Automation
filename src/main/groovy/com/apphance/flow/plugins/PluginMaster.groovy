package com.apphance.flow.plugins

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.detection.project.ProjectType
import com.apphance.flow.detection.project.ProjectTypeDetector
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
import groovy.json.JsonBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logging

import javax.inject.Inject

import static com.apphance.flow.configuration.reader.GradlePropertiesPersister.FLOW_PROP_FILENAME

class PluginMaster {

    def logger = Logging.getLogger(getClass())

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

        String docOutFileName = project.hasProperty('outDocFile') ? project.outDocFile : 'build/doc/doc.json'
        File docOutFile = new File(project.rootDir, docOutFileName)
        Map<Plugin, Set<Task>> doc = [:]

        if (project.hasProperty('docMode')) {
            logger.lifecycle "docMode enabled"
            docOutFile.parentFile.mkdirs()
            if (docOutFile.createNewFile()) logger.lifecycle "${docOutFile.absolutePath} successfully created"
            else logger.error "Error while creating docOutFile.absolutePath"
        }

        def installPlugin = {
            logger.info("Applying plugin $it")

            Plugin<Project> plugin = (Plugin<Project>) injector.getInstance(it)

            Set<Task> tasksBefore = project.tasks.findAll()

            plugin.apply(project)
            doc[plugin] = (project.tasks.findAll() - tasksBefore).findAll { it.enabled }

            project.plugins.add(plugin)
        }

        PLUGINS['COMMON'].each installPlugin

        if (project.file(FLOW_PROP_FILENAME).exists()) {
            PLUGINS[projectType.name()].each installPlugin
        }

        if (project.hasProperty('docMode') && docOutFile.exists()) {
            saveDocInfo(doc, docOutFile)
        }
    }

    void saveDocInfo(Map<Plugin, Set<Task>> map, File file) {
        def json = new JsonBuilder()

        def output = json([plugins: map.collectEntries { Plugin plugin, Set<Task> tasks ->
            [(plugin.class.simpleName): tasks.collect {
                [taskClass: it.class.superclass.simpleName, taskName: it.name, description: it.description]
            }]
        }, configurations: configurations.values().collectEntries {
            [(it.class.simpleName): it.propertyFields.collect {
                [name: it.name, description: it.doc()]
            }]
        }])
        logger.info "Prepared json: " + output.toString()

        file << output.toString()
    }
}
