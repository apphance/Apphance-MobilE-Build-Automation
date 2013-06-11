package com.apphance.flow.configuration.reader

import com.apphance.flow.configuration.AbstractConfiguration
import org.gradle.api.Project

interface PropertyPersister {

    def get(String name)

    def save(Collection<AbstractConfiguration> configurations)

    void init(Project project)
}
