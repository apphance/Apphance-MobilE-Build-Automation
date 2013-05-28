package com.apphance.ameba.configuration

import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.reader.PropertyReader
import com.apphance.ameba.detection.ProjectTypeDetector
import org.gradle.api.Project

import javax.inject.Inject

abstract class ProjectConfiguration extends AbstractConfiguration {

    public static final String TMP_DIR = 'flow-tmp'
    public static final String LOG_DIR = 'flow-log'
    public static final String BUILD_DIR = 'build'

    @Inject Project project
    @Inject PropertyReader reader
    @Inject ProjectTypeDetector projectTypeDetector

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

    File getBuildDir() {
        project.file(BUILD_DIR)
    }

    File getTmpDir() {
        project.file(TMP_DIR)
    }

    File getLogDir() {
        project.file(LOG_DIR)
    }

    File getRootDir() {
        project.rootDir
    }

    abstract Collection<String> getSourceExcludes()
}