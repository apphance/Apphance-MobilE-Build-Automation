package com.apphance.flow.plugins.android.builder

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.plugins.release.FlowArtifact

import javax.inject.Inject

import static com.apphance.flow.configuration.android.AndroidArchiveType.APK
import static com.apphance.flow.configuration.android.AndroidArchiveType.JAR

class AndroidArtifactProvider {

    @Inject AndroidConfiguration conf
    @Inject AndroidReleaseConfiguration releaseConf

    Closure<String> fullVersionString = { conf.fullVersionString }
    Closure<String> projectNameNoWhiteSpace = { conf.projectNameNoWhiteSpace }
    Closure<URL> releaseUrlVersioned = { releaseConf.releaseUrlVersioned }
    Closure<File> releaseDir = { releaseConf.releaseDir }

    AndroidBuilderInfo builderInfo(AndroidVariantConfiguration avc) {
        new AndroidBuilderInfo(
                variant: avc.name,
                mode: avc.mode,
                tmpDir: avc.tmpDir,
                buildDir: avc.buildDir,
                variantDir: avc.variantDir?.value,
                filePrefix: "${projectNameNoWhiteSpace()}-${avc.mode.lowerCase()}-${avc.name}-${fullVersionString()}",
                originalFile: avc.originalFile,
                type: avc.isLibrary() ? JAR : APK
        )
    }

    FlowArtifact artifact(AndroidBuilderInfo abi) {
        def name = "${abi.filePrefix}.${abi.type.lowerCase()}"
        new FlowArtifact(
                name: "${abi.type.name()} ${abi.mode} file for ${abi.variant}",
                url: new URL("${releaseUrlVersioned()}/$name"),
                location: new File(releaseDir(), name)
        )
    }
}
