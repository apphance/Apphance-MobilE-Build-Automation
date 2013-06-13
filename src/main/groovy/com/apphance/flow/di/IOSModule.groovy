package com.apphance.flow.di

import com.apphance.flow.detection.ProjectTypeDetector
import com.apphance.flow.plugins.ios.apphance.tasks.AddIOSApphanceTaskFactory
import com.google.inject.AbstractModule
import com.google.inject.assistedinject.FactoryModuleBuilder
import org.gradle.api.Project

import static com.apphance.flow.detection.ProjectType.IOS

class IOSModule extends AbstractModule {

    private Project project
    private ProjectTypeDetector projectTypeDetector = new ProjectTypeDetector()

    IOSModule(Project project) {
        this.project = project
    }

    @Override
    protected void configure() {
        if (projectTypeDetector.detectProjectType(project.rootDir) == IOS)
            install(new FactoryModuleBuilder().build(AddIOSApphanceTaskFactory))
    }
}
