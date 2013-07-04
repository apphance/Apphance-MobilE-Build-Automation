package com.apphance.flow.plugins.ios.apphance.pbx

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.parsers.PbxJsonParser
import com.google.inject.assistedinject.Assisted
import groovy.json.JsonSlurper
import groovy.transform.PackageScope

import javax.inject.Inject
import java.util.concurrent.atomic.AtomicInteger

import static com.apphance.flow.configuration.apphance.ApphanceLibType.libForMode
import static com.apphance.flow.plugins.ios.parsers.PbxJsonParser.*
import static groovy.json.JsonOutput.toJson
import static java.io.File.createTempFile
import static java.security.MessageDigest.getInstance
import static org.gradle.api.logging.Logging.getLogger

/**
 * Helper parsing PBX project file.
 *
 */
class IOSApphancePbxEnhancer {

    private logger = getLogger(getClass())

    @Inject IOSConfiguration conf
    @Inject IOSExecutor iosExecutor
    @Inject PbxJsonParser pbxJsonParser

    @PackageScope IOSVariant variant
    private AtomicInteger hash = new AtomicInteger()

    @Inject
    IOSApphancePbxEnhancer(@Assisted IOSVariant variant) {
        this.variant = variant
    }

    void addApphanceToPbx() {
        addFrameworks()
        addFlagsAndPaths()
        saveModifiedPbx()
    }

    @PackageScope
    void addFrameworks() {
        frameworksToAdd.findAll { !pbxJsonParser.isFrameworkDeclared(variant.variantPbx, ~/${it.name}/) }.each(this.&addFramework)
    }

    @PackageScope
    void addFramework(Map frameworkDetails) {
        def frameworkHash = hash()
        json.objects[frameworkHash] = [
                isa: PBX_FILE_REFERENCE,
                lastKnownFileType: 'wrapper.framework',
                name: frameworkDetails.name,
                path: frameworkDetails.path,
                sourceTree: frameworkDetails.group,
        ]

        def frameworkFileHash = hash()
        json.objects[frameworkFileHash] = [
                isa: PBX_BUILD_FILE,
                fileRef: frameworkHash,
        ]
        frameworksBuildPhase.files << frameworkFileHash
        mainGroupFrameworks.children << frameworkHash
    }

    @PackageScope
    void addFlagsAndPaths() {
        def bs = configuration.buildSettings as Map

        if (bs.OTHER_LDFLAGS) {
            bs.OTHER_LDFLAGS << '-ObjC'
            bs.OTHER_LDFLAGS << '-all_load'
        } else {
            bs.OTHER_LDFLAGS = ['-ObjC', '-all_load']
        }

        if (bs.FRAMEWORK_SEARCH_PATHS) {
            bs.FRAMEWORK_SEARCH_PATHS << '\"$(SRCROOT)\"'
        } else {
            bs.FRAMEWORK_SEARCH_PATHS = ['$(inherited)', '\"$(SRCROOT)\"']
        }

        if (bs.LIBRARY_SEARCH_PATHS) {
            bs.LIBRARY_SEARCH_PATHS << "\$(SRCROOT)/${frameworkName}.framework"
        } else {
            bs.LIBRARY_SEARCH_PATHS = ['$(inherited)', "\"\$(SRCROOT)/${frameworkName}.framework\""]
        }
    }

    @PackageScope
    List<String> getFilesToReplaceLogs() {
        def allSourceFiles = [:]
        findAllSourceFiles(mainGroup, allSourceFiles, '')
        def sourceFilesForPhase = fileNamesForSourceBuildPhase
        allSourceFiles.findAll { it.key in sourceFilesForPhase }.values() as List<String>
    }

    private void findAllSourceFiles(Map group, Map sourceFiles, String actualPath) {
        String path = group.path ? "$actualPath${group.path}/" : actualPath

        group.children.each { hash ->
            def child = json.objects[hash] as Map
            if (child.isa == PBX_FILE_REFERENCE) {
                sourceFiles.put(child.path, path + child.path)
            } else if (child.isa == PBX_GROUP) {
                findAllSourceFiles(child, sourceFiles, path)
            }
        }
    }

    private List getFileNamesForSourceBuildPhase() {
        def sourcesBuildPhase = json.objects.find {
            it.key in target.buildPhases && it.value.isa == PBX_SOURCES_BUILD_PHASE
        }.value
        def files = sourcesBuildPhase.files
        def fileRefsHashes = json.objects.findAll { it.key in files }*.value.fileRef
        json.objects.findAll { it.key in fileRefsHashes }*.value.path
    }

    @PackageScope
    String getGCCPrefixFilePath() {
        configuration.buildSettings.GCC_PREFIX_HEADER
    }

    @Lazy
    private Map json = {
        new JsonSlurper().parseText(iosExecutor.pbxProjToJSON(variant.variantPbx).join('\n')) as Map
    }()

    @Lazy
    @PackageScope Map target = {
        json.objects.find {
            it.key in rootObject.targets && it.value.isa == PBX_NATIVE_TARGET && it.value.name == variant.target
        }.value as Map
    }()

    @Lazy
    @PackageScope Map rootObject = {
        def rootObjectHash = json.rootObject
        json.objects[rootObjectHash] as Map
    }()

    @Lazy
    @PackageScope Map configuration = {
        def buildConfListHash = target.buildConfigurationList
        def confHashes = json.objects[buildConfListHash].buildConfigurations

        json.objects.find {
            it.key in confHashes && it.value.isa == XCBUILD_CONFIGURATION && it.value.name == variant.configuration
        }.value as Map
    }()

    @Lazy
    @PackageScope Map frameworksBuildPhase = {
        json.objects.find {
            it.key in target.buildPhases && it.value.isa == PBX_FRAMEWORKS_BUILD_PHASE
        }.value as Map
    }()

    @Lazy
    @PackageScope Map mainGroup = {
        rootObject.mainGroup
        json.objects[rootObject.mainGroup] as Map
    }()

    @Lazy
    @PackageScope Map mainGroupFrameworks = {
        def mainGroupHash = rootObject.mainGroup
        def mainGroupChildrenHashes = json.objects[mainGroupHash].children
        json.objects.find {
            it.key in mainGroupChildrenHashes && it.value.name == 'Frameworks' && it.value.isa == PBX_GROUP
        }.value as Map
    }()

    private void saveModifiedPbx() {
        def pbx = new File(variant.tmpDir, "${conf.xcodeDir.value}/project.pbxproj")
        logger.info("Saving PBX with Apphance added to file: $pbx.absolutePath")
        def tmpJson = createTempFile('pbx', 'json')
        tmpJson.text = toJson(json)
        pbx.text = iosExecutor.plistToXML(tmpJson).join('\n')
        tmpJson.delete()
    }

    private String hash() {
        md5(hash.incrementAndGet().toString()).toUpperCase()
    }

    private String md5(String s) {
        def digest = getInstance('MD5')
        digest.update(s.bytes);
        new BigInteger(1, digest.digest()).toString(16).padLeft(32, '0')
    }

    @Lazy
    private frameworksToAdd = [
            ['name': "${frameworkName}.framework", 'path': "${frameworkName}.framework", 'group': '<group>'],
            ['name': 'CoreLocation.framework', 'path': 'System/Library/Frameworks/CoreLocation.framework', 'group': 'SDKROOT'],
            ['name': 'QuartzCore.framework', 'path': 'System/Library/Frameworks/QuartzCore.framework', 'group': 'SDKROOT'],
            ['name': 'SystemConfiguration.framework', 'path': 'System/Library/Frameworks/SystemConfiguration.framework', 'group': 'SDKROOT'],
            ['name': 'CoreTelephony.framework', 'path': 'System/Library/Frameworks/CoreTelephony.framework', 'group': 'SDKROOT'],
            ['name': 'AudioToolbox.framework', 'path': 'System/Library/Frameworks/AudioToolbox.framework', 'group': 'SDKROOT'],
            ['name': 'Security.framework', 'path': 'System/Library/Frameworks/Security.framework', 'group': 'SDKROOT'],
    ]

    private String getFrameworkName() {
        "Apphance-${libForMode(variant.apphanceMode.value).groupName.replace('p', 'P')}"
    }
}
