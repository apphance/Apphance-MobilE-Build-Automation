package com.apphance.ameba.di

import com.apphance.ameba.configuration.AndroidConfiguration
import com.apphance.ameba.configuration.GradlePropertiesPersister
import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.configuration.PropertyPersister
import com.google.inject.AbstractModule

import static com.google.inject.Scopes.SINGLETON

class ConfigurationModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ProjectConfiguration).in(SINGLETON)
        bind(AndroidConfiguration).in(SINGLETON)
        bind(PropertyPersister).to(GradlePropertiesPersister)
    }
}
