package com.apphance.flow.configuration.android

import com.apphance.flow.configuration.ProjectConfiguration
import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.AndroidExecutor
import com.apphance.flow.plugins.android.parsers.AndroidBuildXmlHelper
import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper

import javax.inject.Inject

import static com.apphance.flow.detection.ProjectType.ANDROID
import static com.apphance.flow.plugins.android.release.tasks.UpdateVersionTask.WHITESPACE_PATTERN
import static com.google.common.base.Strings.isNullOrEmpty
import static java.io.File.pathSeparator
import static java.util.ResourceBundle.getBundle
import static org.apache.commons.lang.StringUtils.isNotEmpty

@com.google.inject.Singleton
class AndroidConfiguration extends ProjectConfiguration {

    String configurationName = 'Android Configuration'

    @Inject AndroidBuildXmlHelper buildXmlHelper
    @Inject AndroidManifestHelper manifestHelper
    @Inject AndroidExecutor androidExecutor
    @Inject ApphanceConfiguration apphanceConf

    private Properties androidProperties
    private bundle = getBundle('validation')

    @Override
    @Inject
    void init() {
        super.init()
        readProperties()
    }

    @Override
    boolean isEnabled() {
        projectTypeDetector.detectProjectType(project.rootDir) == ANDROID
    }

    StringProperty projectName = new StringProperty(
            name: 'android.project.name',
            message: 'Project name',
            defaultValue: { defaultName() },
            possibleValues: { possibleNames() },
            required: { true }
    )

    @Override
    String getVersionCode() {
        extVersionCode ?: manifestHelper.readVersion(rootDir).versionCode ?: ''
    }

    @Override
    String getVersionString() {
        extVersionString ?: manifestHelper.readVersion(rootDir).versionString ?: ''
    }

    File getResDir() {
        project.file('res')
    }

    def target = new StringProperty(
            name: 'android.target',
            message: 'Android target',
            defaultValue: { androidProperties.getProperty('target') ?: '' },
            required: { true },
            possibleValues: { possibleTargets() },
            validator: { it in possibleTargets() }
    )

    String getMainPackage() {
        manifestHelper.androidPackage(rootDir)
    }

    File getSDKDir() {
        def androidHome = reader.envVariable('ANDROID_HOME')
        androidHome ? new File(androidHome) : null
    }

    Collection<String> sourceExcludes = super.sourceExcludes + ['**/*.class', '**/bin/**']

    private Collection<File> sdkJarLibs = []

    Collection<File> getSdkJars() {
        if (sdkJarLibs.empty && target.value) {
            def target = target.value
            if (target.startsWith('android')) {
                String version = target.split('-')[1]
                sdkJarLibs << new File(SDKDir, "platforms/android-$version/android.jar")
            } else {
                List splitTarget = target.split(':')
                if (splitTarget.size() > 2) {
                    String version = splitTarget[2]
                    sdkJarLibs << new File(SDKDir, "platforms/android-$version/android.jar")
                    if (target.startsWith('Google')) {
                        def mapJarFiles = new FileNameFinder().getFileNames(SDKDir.canonicalPath,
                                "add-ons/addon*google*apis*google*$version/libs/maps.jar")
                        for (String path in mapJarFiles) {
                            sdkJarLibs << new File(path)
                        }
                    }
                    if (target.startsWith('KYOCERA Corporation:DTS')) {
                        sdkJarLibs << new File(SDKDir, "add-ons/addon_dual_screen_apis_kyocera_corporation_$version/libs/dualscreen.jar")
                    }
                    if (target.startsWith('LGE:Real3D')) {
                        sdkJarLibs << new File(SDKDir, "add-ons/addon_real3d_lge_$version/libs/real3d.jar")
                    }
                    if (target.startsWith('Sony Ericsson Mobile Communications AB:EDK')) {
                        sdkJarLibs << new File(SDKDir, "add-ons/addon_edk_sony_ericsson_mobile_communications_ab_$version/libs/com.sonyericsson.eventstream_1.jar")
                    }
                }
            }
        }
        sdkJarLibs
    }

    private Collection<File> jarLibs

    Collection<File> getJarLibraries() {
        if (!jarLibs || !linkedJarLibs) {
            librariesFinder(rootDir)
        }
        jarLibs
    }

    private Collection<File> linkedJarLibs

    Collection<File> getLinkedJarLibraries() {
        if (!jarLibs || !linkedJarLibs) {
            librariesFinder(rootDir)
        }
        linkedJarLibs
    }

    private Closure librariesFinder = { File projectDir ->
        if (!jarLibs) {
            jarLibs = []
        }
        if (!linkedJarLibs) {
            linkedJarLibs = []
        }
        def libraryDir = new File(projectDir, 'libs')
        if (libraryDir.exists()) {
            jarLibs.addAll(libraryDir.listFiles().findAll(jarMatcher))
        }
        def props = new Properties()
        props.load(new FileInputStream(new File(projectDir, 'project.properties')))
        props.findAll { it.key.startsWith('android.library.reference.') }.each {
            File libraryProject = new File(projectDir, it.value.toString())
            File binProject = new File(libraryProject, 'bin')
            if (binProject.exists()) {
                linkedJarLibs.addAll(binProject.listFiles().findAll(jarMatcher))
            }
            librariesFinder(libraryProject)
        }
    }

    private Closure jarMatcher = { File f ->
        f.name.endsWith('.jar')
    }

    Set<File> getAllJars() {
        [getSdkJars(), getJarLibraries(), getLinkedJarLibraries()].flatten() as Set
    }

    String getAllJarsAsPath() {
        getAllJars().join(pathSeparator)
    }

    private String defaultName() {
        buildXmlHelper.projectName(rootDir)
    }

    private List<String> possibleNames() {
        [rootDir.name, defaultName()].findAll { !it?.trim()?.empty }
    }

    private List<String> possibleTargets() {
        androidExecutor.targets
    }

    def readProperties() {
        androidProperties = new Properties()
        ['local', 'build', 'default', 'project'].each {
            File propFile = project.file("${it}.properties")
            if (propFile?.exists()) {
                androidProperties.load(new FileInputStream(propFile))
            }
        }
    }

    boolean isLibrary() {
        androidProperties.get('android.library') == 'true'
    }

    @Override
    void checkProperties() {
        super.checkProperties()

        check !isNullOrEmpty(reader.envVariable('ANDROID_HOME')), "Environment variable 'ANDROID_HOME' must be set!"
        check rootDir.canWrite(), "No write access to project root dir ${rootDir.absolutePath}, check file system permissions!"
        check !isNullOrEmpty(projectName.value), "Property ${projectName.name} must be set!"
        check versionCode?.matches('[0-9]+'), bundle.getString('exception.android.version.code')
        check((isNotEmpty(versionString) && !WHITESPACE_PATTERN.matcher(versionString).find()), bundle.getString('exception.android.version.string'))
        check target.validator(target.value), "Property ${target.name} must be set!"
        check !isNullOrEmpty(mainPackage), "Property 'package' must be set! Check AndroidManifest.xml file!"
    }
}
