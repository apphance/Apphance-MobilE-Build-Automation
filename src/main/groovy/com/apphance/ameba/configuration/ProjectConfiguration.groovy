package com.apphance.ameba.configuration

import com.apphance.ameba.detection.ProjectType
import com.apphance.ameba.detection.ProjectTypeDetector
import org.gradle.api.Project

import javax.inject.Inject
import java.lang.reflect.Field

class ProjectConfiguration implements Configuration {

    //TODO common getter with exception
    //TODO possible values

    @Inject
    private Project project

    @Inject
    private ProjectTypeDetector typeDetector

    ProjectConfiguration() {
        amebaProperties.each {
            it.value = null
        }
    }

    @AmebaProp(name = 'project.name', message = 'Project name', defaultValue = { 'Sample name' })
    String name

    @AmebaProp(name = 'project.version.code', message = 'Version code', defaultValue = { 0 })
    Long versionCode

    @AmebaProp(
            name = 'project.type',
            message = 'Project type',
            defaultValue = { typeDetector.detectProjectType(project.rootDir) })
    ProjectType projectType

    int order = 10

    @Override
    String getPluginName() {
        "Project plugin"
    }

    def dependsOn = []

//    def props = [
//            new AmebaProperty(name: 'project.name', message: 'Project name', defaultValue: { 'Sample project name' }),
//            new AmebaProperty(name: 'project.version.code', message: 'Version code', defaultValue: { 0 }),
//            new AmebaProperty(name: 'project.version.string', message: 'Version string', defaultValue: { 'NOVERSION' }),
//            new AmebaProperty(name: 'project.type', message: 'Project type', defaultValue: { typeDetector.detectProjectType(project.rootDir) }),
//            new AmebaProperty(name: 'project.log.dir', message: 'Log directory', defaultValue: { project.file('log').canonicalPath }),
//            new AmebaProperty(name: 'project.build.dir', message: 'Build directory', defaultValue: { project.file('build').canonicalPath }),
//            new AmebaProperty(name: 'project.tmp.dir', message: 'Temporary directory', defaultValue: { project.file('tmp').canonicalPath })
//    ]

//    List<AmebaProperty> getAmebaProperties() {
//        props
//    }


    ProjectType getType() {
        ProjectType.valueOf(propertyR('project.type'))
    }

    @Override
    boolean isEnabled() {
        true
    }

    @Override
    void setEnabled(boolean enabled) {
        this.enabled = true
    }

    @Override
    List getAmebaProperties() {
        null
    }


}

