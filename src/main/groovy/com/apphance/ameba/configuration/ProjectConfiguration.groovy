package com.apphance.ameba.configuration

import com.apphance.ameba.detection.ProjectType
import com.apphance.ameba.detection.ProjectTypeDetector
import org.gradle.api.Project

import javax.inject.Inject

@com.google.inject.Singleton
class ProjectConfiguration extends Configuration {

    ProjectConfiguration() {
    }

    //TODO common getter with exception
    @Inject
    private Project project

    @Inject
    private ProjectTypeDetector typeDetector

    def name = new Prop<String>(
            name: 'project.name',
            message: 'Project name',
            defaultValue: { 'Sample name' })

    def versionCode = new Prop<Long>(
            name: 'project.version.code',
            message: 'Version code',
            defaultValue: { 0 })

    def versionString = new Prop<String>(
            name: 'project.version.string',
            message: 'Version string')

    def type = new Prop<ProjectType>(
            name: 'project.type',
            message: 'Project type',
            defaultValue: { typeDetector.detectProjectType(project.rootDir) },
            possibleValues: ProjectType.values())

    def buildDir = new Prop<File>(
            name: 'project.dir.build',
            message: 'Project build directory',
            defaultValue: { project.file('build') })

    def tmpDir = new Prop<File>(
            name: 'project.dir.tmp',
            message: 'Project temporary directory',
            defaultValue: { project.file('tmp') })

    def logDir = new Prop<File>(
            name: 'project.dir.log',
            message: 'Project log directory',
            defaultValue: { project.file('log') })

    int order = 0

    String configurationName = 'Project configuration'

    @Override
    boolean isEnabled() {
        true
    }

    @Override
    void setEnabled(boolean enabled) {
        this.enabled = true
    }

}

