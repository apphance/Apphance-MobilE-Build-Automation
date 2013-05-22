package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.reader.PropertyReader
import com.apphance.ameba.detection.ProjectTypeDetector
import com.apphance.ameba.executor.AndroidExecutor
import com.apphance.ameba.plugins.android.parsers.AndroidBuildXmlHelper
import com.apphance.ameba.plugins.android.parsers.AndroidManifestHelper
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.configuration.release.ReleaseConfiguration.OTA_DIR
import static com.apphance.ameba.detection.ProjectType.ANDROID
import static com.apphance.ameba.plugins.android.release.tasks.UpdateVersionTask.WHITESPACE_PATTERN
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
            possibleValues: { possibleNames() },
            required: { true }
    )

    @Override
    String getVersionCode() {
        extVersionCode ?: manifestHelper.readVersion(rootDir).versionCode ?: ''
    }

    String getExtVersionCode() {
        reader.systemProperty('version.code') ?: reader.envVariable('VERSION_CODE') ?: ''
    }

    @Override
    String getVersionString() {
        extVersionString ?: manifestHelper.readVersion(rootDir).versionString ?: ''
    }

    String getExtVersionString() {
        reader.systemProperty('version.string') ?: reader.envVariable('VERSION_STRING') ?: ''
    }

    @Override
    File getBuildDir() {
        project.file('build')
    }

    @Override
    File getTmpDir() {
        project.file(TMP_DIR)
    }

    @Override
    File getLogDir() {
        project.file(LOG_DIR)
    }

    File getResDir() {
        project.file('res')
    }

    @Override
    File getRootDir() {
        project.rootDir
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

    final Collection<String> sourceExcludes = ['**/*.class', '**/bin/**', '**/build/*', "**/${TMP_DIR}/**/*", "**/${OTA_DIR}/**/*"]

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
        androidExecutor.listTarget(rootDir).findAll { !it?.trim()?.empty }
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
    String getFullVersionString() {
        "${versionString}_${versionCode}"
    }

    @Override
    String getProjectVersionedName() {
        "${projectName.value}-$fullVersionString"
    }

    @Override
    void checkProperties() {
        check !isNullOrEmpty(reader.envVariable('ANDROID_HOME')), "Environment variable 'ANDROID_HOME' must be set!"
        check rootDir.canWrite(), "No write access to project root dir ${rootDir.absolutePath}, check file system permissions!"
        check !isNullOrEmpty(projectName.value), "Property ${projectName.name} must be set!"
        check versionCode?.matches('[0-9]+'), """|Property 'versionCode' must have numerical value! Check 'version.code'
                                                 |system property or 'VERSION_STRING' env variable
                                                 |or AndroidManifest.xml file!""".stripMargin()
        check !WHITESPACE_PATTERN.matcher(versionString ?: '').find(), """|Property 'versionString' must not have
                                                                          |whitespace characters! Check 'version.string'
                                                                          |system property or 'VERSION_STRING' env
                                                                          |variable or AndroidManifest.xml file!"""
                .stripMargin()
        check target.validator(target.value), "Property ${target.name} must be set!"
        check !isNullOrEmpty(mainPackage), "Property 'package' must be set! Check AndroidManifest.xml file!"
    }
}
