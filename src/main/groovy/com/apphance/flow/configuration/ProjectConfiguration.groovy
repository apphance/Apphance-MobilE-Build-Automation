package com.apphance.flow.configuration

import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.configuration.reader.PropertyReader
import com.apphance.flow.detection.project.ProjectTypeDetector
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.flow.configuration.reader.GradlePropertiesPersister.FLOW_PROP_FILENAME
import static com.apphance.flow.configuration.release.ReleaseConfiguration.OTA_DIR

abstract class ProjectConfiguration extends AbstractConfiguration {

    public static final String TMP_DIR = 'flow-tmp'
    public static final String LOG_DIR = 'flow-log'
    public static final String BUILD_DIR = 'build'

    @Inject Project project
    @Inject PropertyReader reader
    @Inject ProjectTypeDetector projectTypeDetector

    abstract String getVersionCode()

    abstract String getVersionString()

    String getExtVersionCode() {
        reader.systemProperty('version.code') ?: reader.envVariable('VERSION_CODE') ?: ''
    }

    String getExtVersionString() {
        reader.systemProperty('version.string') ?: reader.envVariable('VERSION_STRING') ?: ''
    }

    String getFullVersionString() {
        "${versionString}_${versionCode}"
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

    Collection<String> getSourceExcludes() {
        [
                "**/${BUILD_DIR}/**/*",
                "**/${OTA_DIR}/**/*",
                "**/${TMP_DIR}/**/*",
                '**/buildSrc/build/**',
                '**/build.gradle',
                "**/gradle.properties",
                "**/${FLOW_PROP_FILENAME}",
                '**/build/**',
                "**/${LOG_DIR}/**/*",
                '.hgcheck/**',
                '**/.gradle/**',
        ]
    }
}