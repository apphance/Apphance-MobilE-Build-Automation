package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.Configuration
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.LongProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.detection.ProjectTypeDetector
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.detection.ProjectType.ANDROID

@com.google.inject.Singleton
class AndroidConfiguration extends Configuration {

    int order = 1
    String configurationName = 'Android configuration'

    @Inject
    Project project

    @Inject
    ProjectTypeDetector projectTypeDetector

    AndroidConfiguration() {
    }

    @Override
    boolean isEnabled() {
        projectTypeDetector.detectProjectType(project.rootDir) == ANDROID
    }

    @Override
    void setEnabled(boolean enabled) {
        //this configuration is enabled based on project type
    }

    def name = new StringProperty(
            name: 'android.project.name',
            message: 'Project name',
            defaultValue: { 'Sample name' })

    def versionCode = new LongProperty(
            name: 'android.version.code',
            message: 'Version code',
            defaultValue: { 0L })

    def versionString = new StringProperty(
            name: 'android.version.string',
            message: 'Version string')

    def buildDir = new FileProperty(
            name: 'android.dir.build',
            message: 'Project build directory',
            defaultValue: { project.file('build') })

    def tmpDir = new FileProperty(
            name: 'android.dir.tmp',
            message: 'Project temporary directory',
            defaultValue: { project.file('tmp') })

    def logDir = new FileProperty(
            name: 'android.dir.log',
            message: 'Project log directory',
            defaultValue: { project.file('log') })

}
