package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.LongProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.detection.ProjectTypeDetector
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.detection.ProjectType.IOS

@com.google.inject.Singleton
class IOSConfiguration extends AbstractConfiguration {

    @Inject
    Project project

    @Inject
    ProjectTypeDetector projectTypeDetector

    @Override
    boolean isEnabled() {
        projectTypeDetector.detectProjectType(project.rootDir) == IOS
    }

    @Override
    void setEnabled(boolean enabled) {
        //this configuration is enabled based on project type
    }

    @Override
    boolean isActive() {
        return false  //To change body of implemented methods use File | Settings | File Templates.
    }

    def name = new StringProperty(
            name: 'ios.project.name',
            message: 'Project name',
            defaultValue: { 'Sample name' })

    def versionCode = new LongProperty(
            name: 'ios.version.code',
            message: 'Version code',
            defaultValue: { 0L })

    def versionString = new StringProperty(
            name: 'ios.version.string',
            message: 'Version string')

    def buildDir = new FileProperty(
            name: 'ios.dir.build',
            message: 'Project build directory',
            defaultValue: { project.file('build') })

    def tmpDir = new FileProperty(
            name: 'ios.dir.tmp',
            message: 'Project temporary directory',
            defaultValue: { project.file('tmp') })

    def logDir = new FileProperty(
            name: 'ios.dir.log',
            message: 'Project log directory',
            defaultValue: { project.file('log') })

    @Override
    String getConfigurationName() {
        'IOS configuration'
    }
}
