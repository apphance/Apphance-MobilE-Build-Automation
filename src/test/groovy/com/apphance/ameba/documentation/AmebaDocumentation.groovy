package com.apphance.ameba.documentation

import org.gradle.tooling.model.Task;

class PluginDocumentation {
    Class clazz
    String name
    String description
    Map<String, Task> tasks = [:]
    String props = null
    String example = null
    String conventions = null

    String getNiceName() {
        def words = name.split('-')
        if (words[1] == 'ios') {
            words[1] = 'iOS'
        } else {
            words[1] = words[1].charAt(0).toUpperCase().toString() + words[1].substring(1)
        }
        return words[1..-1].join(' ')
    }

    @Override
    public String toString() {
        return this.getProperties()
    }
}

class PluginGroupDocumentation {
    String name
    Map<String, PluginDocumentation> plugins = [:]
    List<String> pluginNames = []

    @Override
    public String toString() {
        return this.getProperties()
    }
}

class AmebaDocumentation {
    Map<String, Task> tasks = [:]
    Map<String, PluginGroupDocumentation> groups = [:]
    List<String> groupNames = []

    @Override
    public String toString() {
        return this.getProperties()
    }
}