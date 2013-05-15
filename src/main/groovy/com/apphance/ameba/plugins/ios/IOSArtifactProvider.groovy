package com.apphance.ameba.plugins.ios

import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant

class IOSArtifactProvider {

//    IOSBuilderInfo buildSingleBuilderInfo(String target, String configuration, String outputDirPostfix, Project project) {
//        IOSBuilderInfo bi = new IOSBuilderInfo(
//                id: "${target}-${configuration}",
//                target: target,
//                configuration: configuration,
//                buildDir: new File(tmpDir(target, configuration), "/build/${configuration}-${outputDirPostfix}"),
//                fullReleaseName: "${target}-${configuration}-${conf.fullVersionString}",
//                filePrefix: "${target}-${configuration}-${conf.fullVersionString}",
//                mobileProvisionFile: parser.findMobileProvisionFile(project, target, configuration, true),
//                plistFile: new File(tmpDir(target, configuration), PropertyCategory.readProperty(project, IOSProjectProperty.PLIST_FILE))
//        )
//        bi
//    }

    IOSBuilderInfo builderInfo(AbstractIOSVariant variant) {
        def bi = new IOSBuilderInfo(
                id: "${variant.target}-${variant.configuration}",
                target: variant.target,
                configuration: variant.configuration,
                buildDir: new File(variant.tmpDir, 'build'),
                fullReleaseName: "${variant.target}-${variant.configuration}-${variant.fullVersionString}",
                filePrefix: "${variant.target}-${variant.configuration}-${variant.fullVersionString}",
                mobileProvisionFile: variant.mobileprovision.value,
                plistFile: variant.plist
        )
        bi
    }
}
