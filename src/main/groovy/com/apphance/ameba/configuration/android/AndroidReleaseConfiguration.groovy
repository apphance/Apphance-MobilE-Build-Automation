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

    static final ICON_PATTERN = /icon.*\.(png|jpg|jpeg|bmp)/
    static final DRAWABLE_DIR_PATTERN = /(drawable-ldpi|drawable-mdpi|drawable-hdpi|drawable-xhdpi|drawable)/

    Map<String, AmebaArtifact> apkFiles = [:]
    Map<String, AmebaArtifact> jarFiles = [:]

    @Inject AndroidManifestHelper manifestHelper
    @Inject AndroidConfiguration androidConf

    @Lazy def files = super.&getFiles.curry(androidConf.resDir, DRAWABLE_DIR_PATTERN)

    @PackageScope
    File defaultIcon() {
        def icon = manifestHelper.readIcon(androidConf.rootDir)?.trim()
        icon ? files { it.name.startsWith(icon) }.find() : null
    }

    @PackageScope
    List<String> possibleIcons() {
        def icons = files { it.name ==~ ICON_PATTERN }*.canonicalPath
        icons.collect { relativeTo(androidConf.rootDir.absolutePath, it).path }
    }

    @Override
    void checkProperties() {
        check !checkException { baseURL }, "Property '${projectURL.name}' is not valid! Should be valid URL address!"
        check language.validator(language.value), "Property '${language.name}' is not valid! Should be two letter lowercase!"
        check country.validator(country.value), "Property '${country.name}' is not valid! Should be two letter uppercase!"
        check releaseMailFrom.validator(releaseMailFrom.value), "Property '${releaseMailFrom.name}' is not valid! Should be valid " +
                "email address! Current value: ${releaseMailFrom.value}"
        check releaseMailTo.validator(releaseMailTo.value), "Property '${releaseMailTo.name}' is not valid! Should be valid email address!  Current value: ${releaseMailTo.value}"
        check releaseMailFlags.validator(releaseMailFlags.persistentForm()), "Property '${releaseMailFlags.name}' is not valid! Possible values: " +
                "${ALL_EMAIL_FLAGS} Current value: ${releaseMailFlags.value}"
        check iconFile.validator(iconFile.value), "Property '${iconFile.name}' (${iconFile.value}) is not valid! Should be existing image file!"
    }
}