package com.apphance.flow.configuration.ios

import com.apphance.flow.configuration.ProjectConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.IOSExecutor

import javax.inject.Inject

import static com.apphance.flow.detection.project.ProjectType.IOS
import static com.apphance.flow.util.file.FileManager.*
import static groovy.io.FileType.DIRECTORIES
import static java.io.File.separator

@com.google.inject.Singleton
class IOSConfiguration extends ProjectConfiguration {

    String configurationName = 'iOS Configuration'

    public static final List<String> FAMILIES = ['iPad', 'iPhone']
    public static final PROJECT_PBXPROJ = 'project.pbxproj'

    @Inject IOSExecutor executor
    @Inject IOSVariantsConfiguration variantsConf

    @Inject
    @Override
    void init() {
        super.init()
    }

    @Override
    String getVersionCode() {
        variantsConf.mainVariant.versionCode
    }

    @Override
    String getVersionString() {
        variantsConf.mainVariant.versionString
    }

    @Override
    StringProperty getProjectName() {
        def sp = new StringProperty() {
            @Override
            String toString() {
                value
            }
        }
        sp.value = variantsConf.mainVariant.projectName
        sp
    }

    def xcodeDir = new FileProperty(
            name: 'ios.dir.xcode',
            message: 'iOS xcodeproj directory',
            possibleValues: { possibleXCodeDirs },
            validator: {
                def file = new File(rootDir, it ? it as String : '')
                file?.absolutePath?.trim() ? (file.exists() && file.isDirectory() && file.name.endsWith('.xcodeproj')) : false
            },
            required: { true }
    )

    @Lazy List<String> possibleXCodeDirs = {
        def dirs = []
        rootDir.traverse(
                type: DIRECTORIES,
                nameFilter: ~/.*\.xcodeproj/,
                excludeFilter: EXCLUDE_FILTER,
                maxDepth: MAX_RECURSION_LEVEL) {
            dirs << relativeTo(rootDir.absolutePath, it.absolutePath).path
        }
        dirs
    }()

    List<String> xcodebuildExecutionPath() {
        xcodeDir.value ? ['xcodebuild', '-project', xcodeDir.value as String] : ['xcodebuild']
    }

    def sdk = new StringProperty(
            name: 'ios.sdk',
            message: 'iOS SDK',
            possibleValues: { executor.sdks as List },
            validator: { it in executor.sdks },
            required: { true }
    )

    def simulatorSdk = new StringProperty(
            name: 'ios.sdk.simulator',
            message: 'iOS simulator SDK',
            possibleValues: { executor.simulatorSdks as List },
            validator: { it in executor.simulatorSdks },
            required: { true }
    )

    List<String> getSchemes() {
        executor.schemes
    }

    @Lazy
    Collection<String> sourceExcludes = { super.sourceExcludes + ["**${separator}$xcodeDir.value${separator}xcuserdata${separator}**"] }()

    @Override
    boolean isEnabled() {
        projectTypeDetector.detectProjectType(project.rootDir) == IOS
    }

    @Override
    void checkProperties() {
        super.checkProperties()

        defaultValidation xcodeDir, sdk, simulatorSdk
    }
}
