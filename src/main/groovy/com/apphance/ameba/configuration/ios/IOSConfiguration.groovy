package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.reader.PropertyReader
import com.apphance.ameba.detection.ProjectTypeDetector
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.detection.ProjectType.IOS

@com.google.inject.Singleton
class IOSConfiguration extends AbstractConfiguration implements ProjectConfiguration {

    String configurationName = 'iOS Configuration'

    //from old conf
    List<String> targets = []
    List<String> configurations = []
    List<String> families = []
    List<String> excludedBuilds = []
    Collection<Expando> allBuildableVariants = []
    String mainTarget
    File distributionDirectory
    File plistFile
    String sdk
    //from old conf

    Project project
    ProjectTypeDetector projectTypeDetector
    PropertyReader reader

    @Inject
    IOSConfiguration(Project project, ProjectTypeDetector projectTypeDetector, PropertyReader reader) {
        this.project = project
        this.projectTypeDetector = projectTypeDetector
        this.reader = reader
    }

    @Override
    String getVersionCode() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    String getExternalVersionCode() {
        reader.systemProperty('version.code') ?: reader.envVariable('VERSION_CODE') ?: ''
    }

    @Override
    String getVersionString() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    String getExternalVersionString() {
        reader.systemProperty('version.string') ?: reader.envVariable('VERSION_STRING') ?: ''
    }

    @Override
    String getFullVersionString() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getProjectVersionedName() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    StringProperty projectName = new StringProperty(
            name: 'ios.project.name',
            message: 'iOS project name',
            defaultValue: { defaultName() },
            possibleValues: { possibleNames() },
            required: { true }
    )

    private String defaultName() {
        ''
    }

    private List<String> possibleNames() {
        [rootDir.name, defaultName()].findAll { !it?.trim()?.empty }
    }

    def plist = new FileProperty(
            name: 'ios.plist.file',
            message: 'iOS project plist file',
            required: { true },
            defaultValue: { null },
            possibleValues: { null },
    )

    @Override
    File getTmpDir() {
        project.file('ameba-tmp')
    }

    @Override
    File getBuildDir() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    File getLogDir() {
        project.file('ameba-log')
    }

    @Override
    File getRootDir() {
        project.rootDir
    }

    @Override
    Collection<String> getSourceExcludes() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    boolean isEnabled() {
        projectTypeDetector.detectProjectType(project.rootDir) == IOS
    }
}
