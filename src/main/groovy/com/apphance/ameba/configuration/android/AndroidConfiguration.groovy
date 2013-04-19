package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.reader.PropertyReader
import com.apphance.ameba.detection.ProjectTypeDetector
import com.apphance.ameba.executor.AndroidExecutor
import com.apphance.ameba.plugins.android.AndroidBuildXmlHelper
import com.apphance.ameba.plugins.android.AndroidManifestHelper
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.detection.ProjectType.ANDROID
import static com.google.common.base.Strings.isNullOrEmpty
import static java.io.File.pathSeparator

@com.google.inject.Singleton
class AndroidConfiguration extends AbstractConfiguration implements ProjectConfiguration {

    String configurationName = 'Android Configuration'

    private Project project
    private ProjectTypeDetector projectTypeDetector
    private AndroidBuildXmlHelper buildXmlHelper
    private AndroidManifestHelper manifestHelper
    private AndroidExecutor androidExecutor
    private PropertyReader reader
    private Properties androidProperties

    @Inject
    AndroidConfiguration(
            Project project,
            AndroidExecutor androidExecutor,
            AndroidManifestHelper manifestHelper,
            AndroidBuildXmlHelper buildXmlHelper,
            ProjectTypeDetector projectTypeDetector,
            PropertyReader reader) {
        this.project = project
        this.androidExecutor = androidExecutor
        this.manifestHelper = manifestHelper
        this.buildXmlHelper = buildXmlHelper
        this.projectTypeDetector = projectTypeDetector
        this.reader = reader

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
            possibleValues: { possibleNames() }
    )

    @Override
    String getVersionCode() {
        reader.systemProperty('version.code') ?:
            reader.envVariable('VERSION_CODE') ?:
                manifestHelper.readVersion(rootDir).versionCode ?:
                    ''
    }

    @Override
    String getVersionString() {
        reader.systemProperty('version.string') ?:
            reader.envVariable('VERSION_STRING') ?:
                manifestHelper.readVersion(rootDir).versionString ?:
                    'git '
    }

    @Override
    File getBuildDir() {
        project.file('build')
    }

    @Override
    File getTmpDir() {
        project.file('ameba-tmp')
    }

    @Override
    File getLogDir() {
        project.file('ameba-log')
    }

    @Override
    File getRootDir() {
        project.rootDir
    }

    def target = new StringProperty(
            name: 'android.target',
            message: 'Android target',
            possibleValues: { possibleTargets() }
    )

    def minTarget = new StringProperty(
            name: 'android.target.min.sdk',
            message: 'Android minimal SDK target'
    )

    def mainPackage = new StringProperty(
            name: 'android.main.package',
            message: 'Android main package',
            defaultValue: { manifestHelper.androidPackage(rootDir) }
    )

    def sdkDir = new FileProperty(
            name: 'android.dir.sdk',
            message: 'Android SDK directory',
            defaultValue: { defaultSDKDir() }
    )

    final Collection<String> sourceExcludes = ['**/*.class', '**/bin/**', '**/build/*']

    private Collection<File> sdkJarLibs = []

    Collection<File> getSdkJars() {
        if (sdkJarLibs.empty && target.value) {
            def sdk = sdkDir.value
            def target = minTarget.value
            if (target.startsWith('android')) {
                String version = target.split('-')[1]
                sdkJarLibs << new File(sdk, "platforms/android-$version/android.jar")
            } else {
                List splitTarget = target.split(':')
                if (splitTarget.size() > 2) {
                    String version = splitTarget[2]
                    sdkJarLibs << new File(sdk, "platforms/android-$version/android.jar")
                    if (target.startsWith('Google')) {
                        def mapJarFiles = new FileNameFinder().getFileNames(sdkDir.value.path,
                                "add-ons/addon*google*apis*google*$version/libs/maps.jar")
                        for (String path in mapJarFiles) {
                            sdkJarLibs << new File(path)
                        }
                    }
                    if (target.startsWith('KYOCERA Corporation:DTS')) {
                        sdkJarLibs << new File(sdk, "add-ons/addon_dual_screen_apis_kyocera_corporation_$version/libs/dualscreen.jar")
                    }
                    if (target.startsWith('LGE:Real3D')) {
                        sdkJarLibs << new File(sdk, "add-ons/addon_real3d_lge_$version/libs/real3d.jar")
                    }
                    if (target.startsWith('Sony Ericsson Mobile Communications AB:EDK')) {
                        sdkJarLibs << new File(sdk, "add-ons/addon_edk_sony_ericsson_mobile_communications_ab_$version/libs/com.sonyericsson.eventstream_1.jar")
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
        Set<File> set = [] as Set
        set.addAll(getSdkJars())
        set.addAll(getJarLibraries())
        set.addAll(getLinkedJarLibraries())
        return set
    }

    String getAllJarsAsPath() {
        getAllJars().join(pathSeparator)
    }

    private String defaultName() {
        buildXmlHelper.projectName(rootDir)
    }

    private List<String> possibleNames() {
        def names = []
        names << rootDir.name
        names << buildXmlHelper.projectName(rootDir)
        names
    }

    private File defaultSDKDir() {
        def androidHome = System.getenv('ANDROID_HOME')
        if (androidHome) {
            return new File(androidHome)
        }
        null
    }

    private List<String> possibleTargets() {
        androidExecutor.listTarget(rootDir)
    }

    def readProperties() {
        this.androidProperties = new Properties()
        if (project != null) {
            ['local', 'build', 'default', 'project'].each {
                File propFile = project.file("${it}.properties")
                if (propFile?.exists()) {
                    this.androidProperties.load(new FileInputStream(propFile))
                }
            }
        }
    }

    boolean isLibrary() {
        androidProperties.get('android.library') == 'true'
    }

    @Override
    String getFullVersionString() {
        "${versionString}_${versionCode}"
    }

    @Override
    String getProjectVersionedName() {
        "${projectName.value}-$fullVersionString"
    }

    @Override
    void checkProperties() {
        check !isNullOrEmpty(target.value), "Property ${target.name} is required"


    }


}
