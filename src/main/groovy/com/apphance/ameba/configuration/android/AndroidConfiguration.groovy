package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.Configuration
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.LongProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.detection.ProjectTypeDetector
import com.apphance.ameba.executor.AndroidExecutor
import com.apphance.ameba.plugins.android.AndroidBuildXmlHelper
import com.apphance.ameba.plugins.android.AndroidManifestHelper
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.detection.ProjectType.ANDROID

@com.google.inject.Singleton
class AndroidConfiguration extends Configuration {

    int order = 1
    String configurationName = 'Android configuration'

    @Inject
    Project project
    @Inject
    ProjectTypeDetector projectTypeDetector
    @Inject
    AndroidBuildXmlHelper buildXmlHelper
    @Inject
    AndroidManifestHelper manifestHelper
    @Inject
    AndroidExecutor androidExecutor

    @Override
    boolean isEnabled() {
        projectTypeDetector.detectProjectType(project.rootDir) == ANDROID
    }

    @Override
    void setEnabled(boolean enabled) {
        //this configuration is enabled based on project type
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

    def buildDir = new FileProperty(
            name: 'android.dir.build',
            message: 'Project build directory',
            defaultValue: { project.file('build') })

    def tmpDir = new FileProperty(
            name: 'android.dir.tmp',
            message: 'Project temporary directory',
            defaultValue: { project.file('tmp') })

    def logDir = new FileProperty(
            name: 'android.dir.log',
            message: 'Project log directory',
            defaultValue: { project.file('log') })

    def rootDir = new FileProperty(
            name: 'android.dir.root',
            message: 'Project root directory',
            defaultValue: { project.rootDir }
    )

    def sdkDir = new FileProperty(
            name: 'android.dir.sdk',
            message: 'Android SDK directory',
    )

    def mainVariant = new StringProperty(
            name: 'android.main.variant',
            message: 'Android main variant'
    )

    def variantsDir = new FileProperty(
            name: 'android.dir.variants',
            message: 'Android variants directory',
            defaultValue: { project.file('variants') }
    )

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
            message: 'Android main package'
    )



    private String defaultName() {
        buildXmlHelper.projectName(project.rootDir)
    }

    private List<String> possibleNames() {
        def names = []
        names << project.rootDir.name
        names << buildXmlHelper.projectName(project.rootDir)
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
}
