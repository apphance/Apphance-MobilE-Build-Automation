package com.apphance.flow.docs

import com.apphance.flow.configuration.AbstractConfiguration
import org.gradle.api.Project

import static groovy.json.JsonOutput.toJson

class DocPluginMasterMixin {

    boolean docModeEnabled(Project project) {
        project.hasProperty('docMode') ? Boolean.valueOf(project.docMode as String) : false
    }

    File docFile(Project project) {
        def name = project.hasProperty('docFile') ? project.docFile : 'build/doc/doc.json'
        def docFile = new File(project.rootDir, name)
        docFile.parentFile.mkdirs()
        docFile.createNewFile()
        docFile
    }

    void saveDoc(Map<Integer, Map> plugins, Map<Integer, AbstractConfiguration> configurations, File file) {
        def json = [:]
        json.plugins = plugins.collectEntries { Integer id, Map details ->
            [
                    (id): [
                            plugin: details.plugin.class.simpleName,
                            tasks: details.tasks.collect {
                                [taskClass: it.class.superclass.simpleName, taskName: it.name, description: it.description]
                            }
                    ]
            ]
        }
        def confs = [:]
        def conf2map = { AbstractConfiguration conf ->
            [
                    conf: conf.configurationName,
                    confClass: conf.class.simpleName,
                    props: [[name: conf.enabledPropKey, description: docBundle.getString(conf.enabledPropKey)]] +
                            conf.propertyFields.collect {
                                [name: it.name, description: it.doc()]
                            }
            ]
        }
        def cnt = 0
        configurations.values().each { c ->
            confs[cnt++] = conf2map(c)
            c.subConfigurations.each { sc ->
                confs[cnt++] = conf2map(sc)
            }
        }
        json.configurations = confs
        file.text = toJson(json)
    }
}
