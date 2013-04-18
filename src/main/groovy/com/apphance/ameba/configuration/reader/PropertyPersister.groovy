package com.apphance.ameba.configuration.reader

import com.apphance.ameba.configuration.AbstractConfiguration
import org.gradle.api.Project

interface PropertyPersister {

    def abstract get(String name)
    def abstract save(Collection<AbstractConfiguration> configurations)
    void init(Project project)
}
