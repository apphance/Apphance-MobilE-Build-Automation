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
    List<String> targets = []
    List<String> configurations = []
    List<String> families = []
    List<String> excludedBuilds = []
    Collection<Expando> allBuildableVariants = []
    List<String> allTargets = []
    List<String> allConfigurations = []
    String mainTarget
    File distributionDir
    File plistFile
    String sdk
    String simulatorSdk

    Boolean isBuildExcluded(String buildName) {
        false
    }

    public static final List<String> FAMILIES = ['iPad', 'iPhone']
    //from old conf

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
        //TODO
        null
    }

    String getExternalVersionCode() {
        reader.systemProperty('version.code') ?: reader.envVariable('VERSION_CODE') ?: ''
    }

    @Override
    String getVersionString() {
        //TODO
        null
    }

    String getExternalVersionString() {
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

    StringProperty projectName = new StringProperty(
            name: 'ios.project.name',
            message: 'iOS project name',
            interactive: { false },
    )

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
        ['xcodebuild', '-project', xcodeDir.value as String]
    }

    Collection<String> sourceExcludes = ['**/build/**']

    @Override
    boolean isEnabled() {
        projectTypeDetector.detectProjectType(project.rootDir) == IOS
    }
}
