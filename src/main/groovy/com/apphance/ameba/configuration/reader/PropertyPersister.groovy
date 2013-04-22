package com.apphance.ameba.configuration.reader

import com.apphance.ameba.configuration.AbstractConfiguration
import org.gradle.api.Project

interface PropertyPersister {

    def get(String name)

    def save(Collection<AbstractConfiguration> configurations)

    void init(Project project)
}
