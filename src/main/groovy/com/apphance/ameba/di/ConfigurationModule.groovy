package com.apphance.ameba.di

import com.apphance.ameba.configuration.*
import com.apphance.ameba.configuration.android.*
import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.ios.IOSReleaseConfiguration
import com.apphance.ameba.configuration.reader.GradlePropertiesPersister
import com.apphance.ameba.configuration.reader.PropertyPersister
import com.apphance.ameba.detection.ProjectTypeDetector
import com.google.inject.AbstractModule
import com.google.inject.multibindings.MapBinder
import org.gradle.api.Project

import static com.apphance.ameba.detection.ProjectType.ANDROID
import static com.apphance.ameba.detection.ProjectType.IOS

class ConfigurationModule extends AbstractModule {

    def configurations = [
            (ANDROID): [
                    AndroidConfiguration,
                    AndroidApphanceConfiguration,
                    AndroidVariantsConfiguration,
                    AndroidReleaseConfiguration,
                    AndroidAnalysisConfiguration,
                    AndroidJarLibraryConfiguration,
                    AndroidTestConfiguration,
            ],
            (IOS): [
                    IOSConfiguration,

            ],
    ]

    def interfaces = [
            (ANDROID): [
                    (ProjectConfiguration): AndroidConfiguration,
                    (ReleaseConfiguration): AndroidReleaseConfiguration
            ],
            (IOS): [
                    (ProjectConfiguration): IOSConfiguration,
                    (ReleaseConfiguration): IOSReleaseConfiguration
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
        MapBinder<Integer, AbstractConfiguration> m = MapBinder.newMapBinder(binder(), Integer, AbstractConfiguration)

        int index = 0

        configurations[typeDetector.detectProjectType(project.rootDir)].each {
            m.addBinding(index++).to(it)
        }

        interfaces[typeDetector.detectProjectType(project.rootDir)].each {
            bind(it.key).to(it.value)
        }

        bind(PropertyPersister).to(GradlePropertiesPersister)
    }
}
