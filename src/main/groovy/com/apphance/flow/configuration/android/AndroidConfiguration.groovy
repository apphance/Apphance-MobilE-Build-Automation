package com.apphance.flow.configuration.android

import com.apphance.flow.configuration.ProjectConfiguration
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.AndroidExecutor
import com.apphance.flow.plugins.android.parsers.AndroidBuildXmlHelper
import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper
import com.google.inject.Singleton

import javax.inject.Inject

import static com.apphance.flow.detection.project.ProjectType.ANDROID
import static com.apphance.flow.plugins.android.release.tasks.UpdateVersionTask.WHITESPACE_PATTERN
import static com.google.common.base.Strings.isNullOrEmpty
import static java.util.ResourceBundle.getBundle
import static org.apache.commons.lang.StringUtils.isNotEmpty

@Singleton
class AndroidConfiguration extends ProjectConfiguration {

    String configurationName = 'Android Configuration'

    @Inject AndroidBuildXmlHelper buildXmlHelper
    @Inject AndroidManifestHelper manifestHelper
    @Inject AndroidExecutor androidExecutor

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

    Collection<String> sourceExcludes = super.sourceExcludes + ['**/*.class', '**/bin/**']

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
