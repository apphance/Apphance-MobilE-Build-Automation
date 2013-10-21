package com.apphance.flow.configuration

import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.configuration.reader.PropertyReader
import com.apphance.flow.detection.project.ProjectTypeDetector
import com.apphance.flow.validation.VersionValidator
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.flow.configuration.reader.GradlePropertiesPersister.FLOW_PROP_FILENAME

abstract class ProjectConfiguration extends AbstractConfiguration {

    public static final String TMP_DIR = 'flow-tmp'
    public static final String LOG_DIR = 'flow-log'
    public static final String BUILD_DIR = 'build'

    @Inject Project project
    @Inject PropertyReader reader
    @Inject ProjectTypeDetector projectTypeDetector
    @Inject VersionValidator versionValidator

    abstract String getVersionCode()

    abstract String getVersionString()

    String getExtVersionCode() {
        reader.envVariable('VERSION_CODE') ?: reader.systemProperty('version.code') ?: ''
    }

    String getExtVersionString() {
        reader.envVariable('VERSION_STRING') ?: reader.systemProperty('version.string') ?: ''
    }

    String getFullVersionString() {
        "${versionString}_${versionCode}"
    }

    abstract StringProperty getProjectName()

    @Lazy
    String projectNameNoWhiteSpace = {
        this.projectName?.value?.replaceAll('\\s', '_')
    }()

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

    Collection<String> getSourceExcludes() {
        [
                'flow-*',
                'buildSrc',
                'build.gradle',
                FLOW_PROP_FILENAME,
                BUILD_DIR,
                '.hgcheck',
                '.git',
                '.gradle',
                'userHome'
        ]
    }
}