package com.apphance.ameba.di

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.configuration.android.*
import com.apphance.ameba.configuration.android.variants.AndroidVariantFactory
import com.apphance.ameba.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.ameba.configuration.apphance.ApphanceConfiguration
import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.ios.IOSFrameworkConfiguration
import com.apphance.ameba.configuration.ios.IOSReleaseConfiguration
import com.apphance.ameba.configuration.ios.IOSUnitTestConfiguration
import com.apphance.ameba.configuration.ios.variants.IOSVariantFactory
import com.apphance.ameba.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.ameba.configuration.reader.GradlePropertiesPersister
import com.apphance.ameba.configuration.reader.PropertyPersister
import com.apphance.ameba.configuration.release.ReleaseConfiguration
import com.apphance.ameba.detection.ProjectTypeDetector
import com.apphance.ameba.plugins.ios.apphance.tasks.AddIOSApphanceTaskFactory
import com.google.inject.AbstractModule
import com.google.inject.assistedinject.FactoryModuleBuilder
import com.google.inject.multibindings.MapBinder
import org.gradle.api.Project

import static com.apphance.ameba.detection.ProjectType.ANDROID
import static com.apphance.ameba.detection.ProjectType.IOS

class ConfigurationModule extends AbstractModule {

    def configurations = [
            (ANDROID): [
                    AndroidConfiguration,
                    ApphanceConfiguration,
                    AndroidVariantsConfiguration,
                    AndroidReleaseConfiguration,
                    AndroidAnalysisConfiguration,
                    AndroidJarLibraryConfiguration,
                    AndroidTestConfiguration,
            ],
            (IOS): [
                    IOSConfiguration,
                    ApphanceConfiguration,
                    IOSReleaseConfiguration,
                    IOSVariantsConfiguration,
                    IOSFrameworkConfiguration,
                    IOSUnitTestConfiguration,
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

    def static variantFactories = [
            (ANDROID): [
                    new FactoryModuleBuilder().build(AndroidVariantFactory)
            ],
            (IOS): [
                    new FactoryModuleBuilder().build(IOSVariantFactory),
                    new FactoryModuleBuilder().build(AddIOSApphanceTaskFactory)
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

        def pt = typeDetector.detectProjectType(project.rootDir)
        def index = 0

        configurations[pt].each {
            m.addBinding(index++).to(it)
        }

        interfaces[pt].each {
            bind(it.key).to(it.value)
        }
        variantFactories[pt].each { install(it) }

        bind(PropertyPersister).to(GradlePropertiesPersister)

    }
}
