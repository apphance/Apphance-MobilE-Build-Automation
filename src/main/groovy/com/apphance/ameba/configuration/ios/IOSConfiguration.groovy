package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.detection.ProjectTypeDetector
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.detection.ProjectType.IOS

@com.google.inject.Singleton
class IOSConfiguration extends AbstractConfiguration implements ProjectConfiguration {

    String configurationName = 'iOS Configuration'

    Project project
    ProjectTypeDetector projectTypeDetector

    @Inject
    IOSConfiguration(Project project, ProjectTypeDetector projectTypeDetector) {
        this.project = project
        this.projectTypeDetector = projectTypeDetector
    }

    @Override
    String getConfigurationName() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getVersionCode() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getVersionString() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getFullVersionString() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    String getProjectVersionedName() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    StringProperty getProjectName() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    File getTmpDir() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    File getBuildDir() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    File getLogDir() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    File getRootDir() {
        return null  //To change body of implemented methods use File | Settings | File Templates.
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
