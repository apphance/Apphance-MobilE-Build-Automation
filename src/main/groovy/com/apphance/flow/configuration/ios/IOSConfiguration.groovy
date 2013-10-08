package com.apphance.flow.configuration.ios

import com.apphance.flow.configuration.ProjectConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.IOSExecutor
import com.google.inject.Singleton

import javax.inject.Inject

import static com.apphance.flow.detection.project.ProjectType.IOS

/**
 * This configuration keeps the values that are passed as a parameters to 'xcodebuild' while building artifacts.
 */
@Singleton
class IOSConfiguration extends ProjectConfiguration {

    String configurationName = 'iOS Configuration'

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

    def sdk = new StringProperty(
            name: 'ios.sdk',
            message: 'iOS SDK',
            doc: { docBundle.getString('ios.sdk') },
            defaultValue: { 'iphoneos' },
            possibleValues: { executor.sdks as List },
            validator: { it in executor.sdks },
            required: { true }
    )

    def simulatorSdk = new StringProperty(
            name: 'ios.sdk.simulator',
            message: 'iOS simulator SDK',
            doc: { docBundle.getString('ios.sdk.simulator') },
            defaultValue: { 'iphonesimulator' },
            possibleValues: { executor.simulatorSdks as List },
            validator: { it in executor.simulatorSdks },
            required: { true }
    )

    List<String> getSchemes() {
        executor.schemes
    }

    @Lazy
    Collection<String> sourceExcludes = { super.sourceExcludes + ["**/xcuserdata/**"] }()

    @Override
    boolean isEnabled() {
        projectTypeDetector.detectProjectType(project.rootDir) == IOS
    }

    @Override
    void checkProperties() {
        defaultValidation sdk, simulatorSdk
    }
}
