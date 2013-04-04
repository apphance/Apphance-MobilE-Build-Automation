package com.apphance.ameba.di

import com.apphance.ameba.configuration.Configuration
import com.apphance.ameba.configuration.GradlePropertiesPersister
import com.apphance.ameba.configuration.PropertyPersister
import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.detection.ProjectTypeDetector
import com.google.inject.AbstractModule
import com.google.inject.multibindings.MapBinder
import org.gradle.api.Project

import static com.apphance.ameba.detection.ProjectType.ANDROID
import static com.apphance.ameba.detection.ProjectType.IOS

class ConfigurationModule extends AbstractModule {

    static configurations = [
            (ANDROID): [
                    AndroidConfiguration,
            ],
            (IOS): [
                    IOSConfiguration,
            ],
    ]

    private Project project

    @groovy.transform.PackageScope
    ProjectTypeDetector typeDetector = new ProjectTypeDetector()

    ConfigurationModule(Project project) {
        this.project = project
    }

    @Override
    protected void configure() {
        MapBinder<Integer, Configuration> m = MapBinder.newMapBinder(binder(), Integer, Configuration)

        int index = 0

        configurations[typeDetector.detectProjectType(project.rootDir)].each {
            m.addBinding(index++).to(it)
        }

        bind(PropertyPersister).to(GradlePropertiesPersister)
    }
}
