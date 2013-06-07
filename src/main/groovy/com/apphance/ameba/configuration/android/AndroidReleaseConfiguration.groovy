package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.release.ReleaseConfiguration
import com.apphance.ameba.plugins.android.parsers.AndroidManifestHelper
import com.apphance.ameba.plugins.release.AmebaArtifact
import groovy.transform.PackageScope

import javax.inject.Inject

import static com.apphance.ameba.util.file.FileManager.relativeTo

/**
 * Keeps configuration for android release.
 */
@com.google.inject.Singleton
class AndroidReleaseConfiguration extends ReleaseConfiguration {

    static final ANDROID_ICON_PATTERN = /icon.*\.(png|jpg|jpeg|bmp)/
    static final DRAWABLE_DIR_PATTERN = /drawable(-ldpi|-mdpi|-hdpi|-xhdpi|)/

    Map<String, AmebaArtifact> apkFiles = [:]
    Map<String, AmebaArtifact> jarFiles = [:]

    @Inject AndroidManifestHelper manifestHelper
    @Inject AndroidConfiguration androidConf
    @Inject AndroidJarLibraryConfiguration jarLibraryConf

    @Lazy def files = super.&getFiles.curry(androidConf.resDir, DRAWABLE_DIR_PATTERN)

    @PackageScope
    File defaultIcon() {
        def icon = manifestHelper.readIcon(androidConf.rootDir)?.trim()
        icon ? files { it.name.startsWith(icon) }.find() : null
    }

    @PackageScope
    List<String> possibleIcons() {
        def icons = files { it.name ==~ ANDROID_ICON_PATTERN }*.canonicalPath
        icons.collect { relativeTo(androidConf.rootDir.absolutePath, it).path }
    }

    @Override
    boolean canBeEnabled() {
        !jarLibraryConf.enabled
    }

    @Override
    String getMessage() {
        "'$configurationName' cannot be enabled because '${jarLibraryConf.configurationName}' is enabled and those plugins are mutually exclusive.\n"
    }

    @Override
    void checkProperties() {
        check !jarLibraryConf.enabled, message
    }
}