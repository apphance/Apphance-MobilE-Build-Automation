package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.reader.PropertyReader
import com.apphance.ameba.detection.ProjectTypeDetector
import com.apphance.ameba.executor.IOSExecutor
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.detection.ProjectType.IOS
import static com.apphance.ameba.util.file.FileManager.MAX_RECURSION_LEVEL
import static groovy.io.FileType.DIRECTORIES
import static java.io.File.separator

@com.google.inject.Singleton
class IOSConfiguration extends AbstractConfiguration implements ProjectConfiguration {

    String configurationName = 'iOS Configuration'

    //from old conf
    List<String> families = []
    Collection<Expando> allBuildableVariants = []
    List<String> allTargets = []
    String mainTarget
    File distributionDir
    File plistFile

    Boolean isBuildExcluded(String buildName) {
        false
    }

    public static final List<String> FAMILIES = ['iPad', 'iPhone']
    //from old conf

    public static final PROJECT_PBXPROJ = 'project.pbxproj'

    @Inject
    Project project
    @Inject
    ProjectTypeDetector projectTypeDetector
    @Inject
    PropertyReader reader
    @Inject
    IOSExecutor executor

    @Override
    String getVersionCode() {
        throw new UnsupportedOperationException('not yet implemented')
    }

    String getExtVersionCode() {
        reader.systemProperty('version.code') ?: reader.envVariable('VERSION_CODE') ?: ''
    }

    @Override
    String getVersionString() {
        throw new UnsupportedOperationException('not yet implemented')
    }

    String getExtVersionString() {
        reader.systemProperty('version.string') ?: reader.envVariable('VERSION_STRING') ?: ''
    }

    @Override
    String getFullVersionString() {
        "${versionString}_${versionCode}"
    }

    @Override
    String getProjectVersionedName() {
        "${projectName.value}-$fullVersionString"
    }

    StringProperty getProjectName() {
        throw new UnsupportedOperationException('not yet implemented')
    }

    @Override
    File getTmpDir() {
        project.file('ameba-tmp')
    }

    @Override
    File getBuildDir() {
        project.file('build')
    }

    @Override
    File getLogDir() {
        project.file('ameba-log')
    }

    @Override
    File getRootDir() {
        project.rootDir
    }

    File getSchemesDir() {
        new File(xcodeDir.value, "xcshareddata${separator}xcschemes")
    }

    def xcodeDir = new FileProperty(
            name: 'ios.dir.xcode',
            message: 'iOS xcodeproj directory',
            possibleValues: { possibleXCodeDirs() as List<String> },
            validator: {
                def file = new File(rootDir, (it?.trim() ?: '') as String)
                file?.absolutePath?.trim() ? (file.exists() && file.isDirectory() && file.name.endsWith('.xcodeproj')) : false
            },
            required: { true }
    )

    private List<String> possibleXCodeDirs() {
        def dirs = []
        rootDir.traverse(type: DIRECTORIES, nameFilter: ~/.*\.xcodeproj/, maxDepth: MAX_RECURSION_LEVEL) {
            dirs << it.absolutePath.replaceAll("${rootDir.absolutePath}${separator}", '')
        }
        dirs
    }

    List<String> xcodebuildExecutionPath() {
        xcodeDir.value ? ['xcodebuild', '-project', xcodeDir.value as String] : ['xcodebuild']
    }

    def sdk = new StringProperty(
            name: 'ios.sdk',
            message: 'iOS SDK',
            possibleValues: { executor.sdks() },
            validator: { it in executor.sdks() },
            required: { true }
    )

    def simulatorSdk = new StringProperty(
            name: 'ios.sdk.simulator',
            message: 'iOS simulator SDK',
            possibleValues: { executor.simulatorSdks() },
            validator: { it in executor.simulatorSdks() },
            required: { true }
    )

    List<String> getTargets() {
        executor.targets()
    }

    List<String> getConfigurations() {
        executor.configurations()
    }

    List<String> getSchemes() {
        executor.schemes()
    }

    Collection<String> sourceExcludes = ['**/build/**']

    @Override
    boolean isEnabled() {
        projectTypeDetector.detectProjectType(project.rootDir) == IOS
    }
}
