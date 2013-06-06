package com.apphance.ameba.di

import com.google.inject.AbstractModule
import org.gradle.api.Project

class GradleModule extends AbstractModule {

    private Project project

    GradleModule(Project project) {
        this.project = project
    }

    @Override
    protected void configure() {
        bind(Project).toInstance(project)
        bind(org.gradle.api.AntBuilder).toInstance(project.ant)
    }
}
