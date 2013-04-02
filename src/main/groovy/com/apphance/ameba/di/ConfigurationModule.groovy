package com.apphance.ameba.di

import com.apphance.ameba.configuration.Configuration
import com.apphance.ameba.configuration.GradlePropertiesPersister
import com.apphance.ameba.configuration.PropertyPersister
import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder

class ConfigurationModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<Configuration> multibinder = Multibinder.newSetBinder(binder(), Configuration);
        multibinder.addBinding().to(IOSConfiguration)
        multibinder.addBinding().to(AndroidConfiguration)

        bind(PropertyPersister).to(GradlePropertiesPersister)
    }
}
