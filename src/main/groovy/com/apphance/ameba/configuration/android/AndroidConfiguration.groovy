package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.Configuration
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.LongProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.detection.ProjectTypeDetector
import com.apphance.ameba.executor.AndroidExecutor
import com.apphance.ameba.plugins.android.AndroidBuildXmlHelper
import com.apphance.ameba.plugins.android.AndroidManifestHelper
import org.gradle.api.GradleException
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.detection.ProjectType.ANDROID
import static java.io.File.pathSeparator

@com.google.inject.Singleton
class AndroidConfiguration extends Configuration {

    String configurationName = 'Android Configuration'

    private Project project
    private ProjectTypeDetector projectTypeDetector
    private AndroidBuildXmlHelper buildXmlHelper
    private AndroidManifestHelper manifestHelper
    private AndroidExecutor androidExecutor
    private Properties androidProperties

    @Inject
    AndroidConfiguration(
            Project project,
            AndroidExecutor androidExecutor,
            AndroidManifestHelper manifestHelper,
            AndroidBuildXmlHelper buildXmlHelper,
            ProjectTypeDetector projectTypeDetector) {
        this.project = project
        this.androidExecutor = androidExecutor
        this.manifestHelper = manifestHelper
        this.buildXmlHelper = buildXmlHelper
        this.projectTypeDetector = projectTypeDetector

        readProperties()
    }
    
    @Override
    boolean isActive() {
        projectTypeDetector.detectProjectType(project.rootDir) == ANDROID
    }

    @Override
    boolean isEnabled() {
        projectTypeDetector.detectProjectType(project.rootDir) == ANDROID
    }

    def projectName = new StringProperty(
            name: 'android.project.name',
            message: 'Project name',
            defaultValue: { defaultName() },
            possibleValues: { possibleNames() }
    )

    def versionCode = new LongProperty(
            name: 'android.version.code',
            message: 'Version code',
            defaultValue: { manifestHelper.readVersion(project.rootDir).versionCode as Long })

    def versionString = new StringProperty(
            name: 'android.version.string',
            message: 'Version string',
            defaultValue: { manifestHelper.readVersion(project.rootDir).versionString })

    //TODO dynamic
    def buildDir = new FileProperty(
            name: 'android.dir.build',
            message: 'Project build directory',
            defaultValue: { project.file('build') },
            askUser: { false })

    //TODO dynamic
    def tmpDir = new FileProperty(
            name: 'android.dir.tmp',
            message: 'Project temporary directory',
            defaultValue: { project.file('tmp') },
            askUser: { false })

    //TODO dynamic
    def logDir = new FileProperty(
            name: 'android.dir.log',
            message: 'Project log directory',
            defaultValue: { project.file('log') },
            askUser: { false })

    //TODO dynamic
    def rootDir = new FileProperty(
            name: 'android.dir.root',
            message: 'Project root directory',
            defaultValue: { project.rootDir },
            askUser: { false })

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
            defaultValue: { manifestHelper.androidPackage(project.rootDir) }
    )

    def sdkDir = new FileProperty(
            name: 'android.dir.sdk',
            message: 'Android SDK directory',
            defaultValue: { defaultSDKDir() }
    )

    final Collection<String> sourceExcludes = ['**/*.class', '**/bin/**', '**/build/*']

    private Collection<File> sdkJarLibs = []

    Collection<File> getSdkJars() {
        if (sdkJarLibs.empty) {
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
            librariesFinder(project.rootDir)
        }
        jarLibs
    }

    private Collection<File> linkedJarLibs

    Collection<File> getLinkedJarLibraries() {
        if (!jarLibs || !linkedJarLibs) {
            librariesFinder(project.rootDir)
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
        buildXmlHelper.projectName(project.rootDir)
    }

    private List<String> possibleNames() {
        def names = []
        names << project.rootDir.name
        names << buildXmlHelper.projectName(project.rootDir)
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
        parseTargets(androidExecutor.listTarget(project.rootDir))
    }

    private List<String> parseTargets(List<String> input) {
        def targets = []
        def targetPattern = /id:.*"(.*)"/
        def targetPrefix = 'id:'
        input.each {
            def targetMatcher = (it =~ targetPattern)
            if (it.startsWith(targetPrefix) && targetMatcher.matches()) {
                targets << targetMatcher[0][1]
            }
        }
        targets.sort()
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
}
