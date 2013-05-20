package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.release.ReleaseConfiguration
import com.apphance.ameba.plugins.android.parsers.AndroidManifestHelper
import com.apphance.ameba.plugins.release.AmebaArtifact

import javax.inject.Inject

/**
 * Keeps configuration for android release.
 */
@com.google.inject.Singleton
class AndroidReleaseConfiguration extends ReleaseConfiguration {

    static final ICON_PATTERN = /icon.*\.(png|jpg|jpeg|bmp)/
    static final DRAWABLE_DIR_PATTERN = /(drawable-ldpi|drawable-mdpi|drawable-hdpi|drawable-xhdpi|drawable)/

    Map<String, AmebaArtifact> apkFiles = [:]
    Map<String, AmebaArtifact> jarFiles = [:]

    @Inject
    AndroidManifestHelper manifestHelper

    @groovy.transform.PackageScope
    File defaultIcon() {
        def icon = manifestHelper.readIcon(conf.rootDir)?.trim()
        def icons = []
        if (icon) {
            conf().resDir.eachDirMatch(~DRAWABLE_DIR_PATTERN) { dir ->
                icons.addAll(dir.listFiles([accept: { it.name.startsWith(icon) }] as FileFilter)*.canonicalFile)
            }
        }
        icons.size() > 0 ? icons.sort()[1] as File : null
    }

    @groovy.transform.PackageScope
    List<String> possibleIcons() {
        List<String> icons = []
        conf().resDir.eachDirMatch(~DRAWABLE_DIR_PATTERN) { dir ->
            icons.addAll(dir.listFiles([accept: { (it.name =~ ICON_PATTERN).matches() }] as FileFilter)*.canonicalPath)
        }
        icons.collect { it.replaceAll("${conf.rootDir.absolutePath}/", '') }.findAll { !it?.trim()?.empty }
    }

    private AndroidConfiguration conf() {
        (AndroidConfiguration) conf
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