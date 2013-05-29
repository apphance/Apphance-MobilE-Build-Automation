package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.executor.IOSExecutor

import javax.inject.Inject

import static com.apphance.ameba.detection.ProjectType.IOS
import static com.apphance.ameba.util.file.FileManager.MAX_RECURSION_LEVEL
import static com.apphance.ameba.util.file.FileManager.relativeTo
import static groovy.io.FileType.DIRECTORIES
import static java.io.File.separator

@com.google.inject.Singleton
class IOSConfiguration extends ProjectConfiguration {

    String configurationName = 'iOS Configuration'

    public static final List<String> FAMILIES = ['iPad', 'iPhone']
    public static final PROJECT_PBXPROJ = 'project.pbxproj'

    @Inject IOSExecutor executor
    @Inject IOSVariantsConfiguration iosVariantsConf

    @Inject
    @Override
    void init() {
        super.init()
    }

    @Override
    String getVersionCode() {
        iosVariantsConf.mainVariant.versionCode
    }

    @Override
    String getVersionString() {
        iosVariantsConf.mainVariant.versionString
    }

    @Override
    StringProperty getProjectName() {
        def sp = new StringProperty() {
            @Override
            String toString() {
                value
            }
        }
        sp.value = iosVariantsConf.mainVariant.projectName
        sp
    }

    @Lazy File schemesDir = {
        new File(xcodeDir.value, "xcshareddata${separator}xcschemes")
    }()

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
                excludeFilter: ~/.*${TMP_DIR}.*/,
                maxDepth: MAX_RECURSION_LEVEL) {
            dirs << relativeTo(rootDir.absolutePath, it.absolutePath).path
        }
        dirs
    }()

    @Lazy List targetConfigurationMatrix = { [targets, configurations].combinations().sort() }()

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

    List<String> getTargets() {
        executor.targets
    }

    List<String> getConfigurations() {
        executor.configurations
    }

    List<String> getSchemes() {
        executor.schemes
    }

    Collection<String> sourceExcludes = ['**/build/**']

    @Override
    boolean isEnabled() {
        projectTypeDetector.detectProjectType(project.rootDir) == IOS
    }

    @Override
    void checkProperties() {
        defaultValidation xcodeDir, sdk, simulatorSdk
    }
}
