package com.apphance.ameba.plugins.core

import org.gradle.api.plugins.PluginContainer

class PluginContainerExtender {

    void extendPluginContainer() {
        PluginContainer.class.metaClass.getImplementationsOf = { clazz ->
            delegate.findAll{clazz.isAssignableFrom(it.class)}
        }
    }
}
