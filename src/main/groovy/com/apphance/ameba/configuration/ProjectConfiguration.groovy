package com.apphance.ameba.configuration

import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.reader.PropertyReader
import org.gradle.api.Project

import javax.inject.Inject

abstract class ProjectConfiguration extends AbstractConfiguration {

    @Inject
    Project project
    @Inject
    PropertyReader reader

    abstract String getVersionCode()

    abstract String getVersionString()

    final String getExtVersionCode() {
        reader.systemProperty('version.code') ?: reader.envVariable('VERSION_CODE') ?: ''
    }

    final String getExtVersionString() {
        reader.systemProperty('version.string') ?: reader.envVariable('VERSION_STRING') ?: ''
    }

    final String getFullVersionString() {
        "${versionString}_${versionCode}"
    }

    final String getProjectVersionedName() {
        "${projectName.value}-$fullVersionString"
    }

    abstract StringProperty getProjectName()

    final File getBuildDir() {
        project.file('build')
    }

    final File getTmpDir() {
        project.file('ameba-tmp')
    }

    final File getLogDir() {
        project.file('ameba-log')
    }

    final File getRootDir() {
        project.rootDir
    }

    abstract Collection<String> getSourceExcludes()
}