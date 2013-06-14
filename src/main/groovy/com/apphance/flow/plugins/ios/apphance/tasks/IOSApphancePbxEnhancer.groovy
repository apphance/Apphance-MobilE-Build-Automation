package com.apphance.flow.plugins.ios.apphance.tasks

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.executor.IOSExecutor
import com.google.inject.assistedinject.Assisted
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.PackageScope

import javax.inject.Inject

import static com.apphance.flow.configuration.apphance.ApphanceLibType.libForMode
import static com.apphance.flow.plugins.ios.parsers.PbxJsonParser.*
import static org.gradle.api.logging.Logging.getLogger

/**
 * Helper parsing PBX project file.
 *
 */
class IOSApphancePbxEnhancer {

    private logger = getLogger(getClass())

    //TODO convert all to lazy fields

    @Inject IOSConfiguration conf
    @Inject IOSExecutor iosExecutor

    @PackageScope
    AbstractIOSVariant variant

    IOSApphancePbxEnhancer(@Assisted AbstractIOSVariant variant) {
        this.variant = variant
    }

    void addApphanceToPbx() {
        def buildPhases = target.value.buildPhases

        addFrameworks()
        replaceLogs()
        addFlagsAndPaths()

        saveModifiedPbx(json)
    }

    @PackageScope
    void addFrameworks() {
        //TODO add frameworks
        def frameworksBuildPhase = json.objects.find {
            it.key in target.buildPhases && it.value.isa == PBXFRAMEWORKS_BUILD_PHASE
        }
    }

    @PackageScope
    void replaceLogs() {
        //TODO add sources
        def sourcesBuildPhase = json.objects.find {
            it.key in target.buildPhases && it.value.isa == PBXSOURCES_BUILD_PHASE
        }
    }

    @PackageScope
    void addFlagsAndPaths() {
        //To change body of created methods use File | Settings | File Templates.
    }

    @Lazy
    @PackageScope Map json = {
        new JsonSlurper().parseText(iosExecutor.pbxProjToJSON.join('\n')) as Map
    }()

    @Lazy
    @PackageScope Map target = {
        def targetsHashes = rootObject.targets
        json.objects.find {
            it.key in targetsHashes && it.value.isa == PBXNATIVE_TARGET && it.value.name == variant.target
        }.value as Map
    }()

    @Lazy
    @PackageScope Map rootObject = {
        def rootObjectHash = json.rootObject
        json.objects.find {
            it.key == rootObjectHash
        }.value as Map
    }()

    @Lazy
    @PackageScope Map configuration = {
        def buildConfigurationListHash = target.buildConfigurationList
        def configurationsHashes = json.objects.find {
            it.key == buildConfigurationListHash
        }.value.buildConfigurations

        json.objects.find {
            it.key in configurationsHashes && it.value.isa == XCBUILD_CONFIGURATION && it.value.name == variant.configuration
        }.value as Map
    }()

    private void saveModifiedPbx(Map pbxJSON) {
        def output = new File(variant.tmpDir, "${conf.xcodeDir.value}/project.pbxjproj")
        logger.info("Saving PBX with Apphance added to file: $output.absolutePath")
        output.text = JsonOutput.prettyPrint(pbxJSON.toString())
    }

    @Lazy
    private frameworksToAdd = [
            ['name': "${frameworkName}.framework", 'path': "${frameworkName}.framework", 'group': '<group>', 'searchName': 'apphance', 'strong': 'Required'],
            ['name': 'CoreLocation.framework', 'path': 'System/Library/Frameworks/CoreLocation.framework', 'group': 'SDKROOT', 'searchName': 'corelocation.framework', 'strong': 'Required'],
            ['name': 'QuartzCore.framework', 'path': 'System/Library/Frameworks/QuartzCore.framework', 'group': 'SDKROOT', 'searchName': 'quartzcore.framework', 'strong': 'Required'],
            ['name': 'SystemConfiguration.framework', 'path': 'System/Library/Frameworks/SystemConfiguration.framework', 'group': 'SDKROOT', 'searchName': 'systemconfiguration.framework', 'strong': 'Weak'],
            ['name': 'CoreTelephony.framework', 'path': 'System/Library/Frameworks/CoreTelephony.framework', 'group': 'SDKROOT', 'searchName': 'coretelephony.framework', 'strong': 'Weak'],
            ['name': 'AudioToolbox.framework', 'path': 'System/Library/Frameworks/AudioToolbox.framework', 'group': 'SDKROOT', 'searchName': 'audiotoolbox.framework', 'strong': 'Required'],
            ['name': 'Security.framework', 'path': 'System/Library/Frameworks/Security.framework', 'group': 'SDKROOT', 'searchName': 'security.framework', 'strong': 'Required']
    ]

    private String getFrameworkName() {
        "Apphance-${libForMode(variant.apphanceMode.value).groupName.replace('p', 'P')}"
    }


}
