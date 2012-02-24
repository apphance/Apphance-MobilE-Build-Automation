package com.apphance.ameba.documentation

import org.gradle.tooling.model.Task;

class PluginDocumentation {
    Class clazz
    String name
    String description
    Map<String,Task> tasks = [:]
    List<String> props = []
    List<String> conventions = []
    List<String> example = []

    @Override
    public String toString() {
        return this.getProperties()
    }
}

class PluginGroupDocumentation {
    String name
    Map<String,PluginDocumentation> plugins = [:]
    List<String> pluginNames = []

    @Override
    public String toString() {
        return this.getProperties()
    }
}

class AmebaDocumentation {
    Map<String,Task> tasks = [:]
    Map<String, PluginGroupDocumentation> groups = [:]
    List<String> groupNames = []

    @Override
    public String toString() {
        return this.getProperties()
    }
}