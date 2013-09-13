package com.apphance.flow.configuration.android

import com.apphance.flow.configuration.release.ReleaseConfiguration
import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper
import com.apphance.flow.plugins.release.FlowArtifact
import com.apphance.flow.util.FlowUtils
import com.google.inject.Singleton
import groovy.transform.PackageScope

import javax.inject.Inject

import static com.apphance.flow.util.file.FileManager.relativeTo
import static org.apache.commons.io.FilenameUtils.removeExtension

@Singleton
@Mixin(FlowUtils)
class AndroidReleaseConfiguration extends ReleaseConfiguration {

    static final ANDROID_ICON_PATTERN = /icon.*\.(png|jpg|jpeg|bmp)/
    static final DRAWABLE_DIR_PATTERN = /drawable(-ldpi|-mdpi|-hdpi|-xhdpi|)/

    Map<String, FlowArtifact> artifacts = [:]

    File androidIcon = copy("/com/apphance/flow/configuration/android/Android_Robot_200.png", new File(temporaryDir, 'defaultIcon.png'))

    @Inject AndroidManifestHelper manifestHelper
    @Inject AndroidConfiguration androidConf

    @Inject
    void releaseIconDefault() {
        if (!releaseIcon.value) {
            releaseIcon.setValue(relativeTo(androidConf.rootDir, androidIcon))
        }
    }

    static Closure ICON_ORDER = {
        ['ldpi', 'mdpi', 'hdpi', 'xhdpi'].findIndexOf { dpi -> it.contains(dpi) }
    }

    @Lazy
    def files = super.&getFiles.curry(androidConf.resDir, DRAWABLE_DIR_PATTERN)

    @PackageScope
    File defaultIcon() {
        def icon = manifestHelper.readIcon(androidConf.rootDir)?.trim()
        List<File> icons = files { File it -> removeExtension(it.name) == icon }
        icon ? icons.sort { ICON_ORDER(it.absolutePath) }.reverse().find() : null
    }

    @PackageScope
    List<String> possibleIcons() {
        def icons = files { it.name ==~ ANDROID_ICON_PATTERN }*.canonicalPath
        icons.collect { relativeTo(androidConf.rootDir.absolutePath, it).path }
    }
}