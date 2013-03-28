package com.apphance.ameba.configuration

import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.LongProperty
import com.apphance.ameba.configuration.properties.ProjectTypeProperty
import com.apphance.ameba.configuration.properties.StringProperty
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

    def name = new StringProperty(
            name: 'project.name',
            message: 'Project name',
            defaultValue: { 'Sample name' })

    def versionCode = new LongProperty(
            name: 'project.version.code',
            message: 'Version code',
            defaultValue: { 0L })

    def versionString = new StringProperty(
            name: 'project.version.string',
            message: 'Version string')

    def type = new ProjectTypeProperty(
            name: 'project.type',
            message: 'Project type',
            defaultValue: { typeDetector.detectProjectType(project.rootDir) },
            possibleValues: ProjectType.values()*.name(),
            validator: { it in ProjectType.values()*.name() })

    def buildDir = new FileProperty(
            name: 'project.dir.build',
            message: 'Project build directory',
            defaultValue: { project.file('build') })

    def tmpDir = new FileProperty(
            name: 'project.dir.tmp',
            message: 'Project temporary directory',
            defaultValue: { project.file('tmp') })

    def logDir = new FileProperty(
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

