package com.apphance.ameba.unit

import org.gradle.tooling.model.Task;

class Plugin {
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

class PluginGroup {
    String name
    Map<String,Plugin> plugins = [:]
    List<String> pluginNames = []

    @Override
    public String toString() {
        return this.getProperties()
    }
}

class DocumentationInfo {
    Map<String,Task> tasks = [:]
    Map<String, PluginGroup> groups = [:]
    List<String> groupNames = []

    @Override
    public String toString() {
        return this.getProperties()
    }
}