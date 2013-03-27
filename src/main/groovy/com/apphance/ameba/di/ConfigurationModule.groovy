package com.apphance.ameba.di

import com.apphance.ameba.configuration.*
import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder

class ConfigurationModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<Configuration> multibinder = Multibinder.newSetBinder(binder(), Configuration);
        multibinder.addBinding().to(ProjectConfiguration)
        multibinder.addBinding().to(AndroidConfiguration)

        bind(PropertyPersister).to(GradlePropertiesPersister)
    }
}
