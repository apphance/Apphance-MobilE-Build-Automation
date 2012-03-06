package com.apphance.ameba.ios.plugins.buildplugin

import groovy.text.SimpleTemplateEngine
import groovy.util.AntBuilder

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AmebaArtifact
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSArtifactBuilderInfo;
import com.apphance.ameba.ios.IOSXCodeOutputParser;
import com.apphance.ameba.ios.IOSProjectConfiguration;
import com.apphance.ameba.ios.MPParser

class IOSSingleReleaseBuilder {

    static Logger logger = Logging.getLogger(IOSSingleReleaseBuilder.class)
    ProjectHelper projectHelper
    ProjectConfiguration conf
    IOSXCodeOutputParser iosConfigurationAndTargetRetriever
    IOSProjectConfiguration iosConf
    AntBuilder ant

    IOSSingleReleaseBuilder(Project project, AntBuilder ant) {
        use (PropertyCategory) {
            this.projectHelper = new ProjectHelper()
            this.conf = project.getProjectConfiguration()
            this.iosConfigurationAndTargetRetriever = new IOSXCodeOutputParser()
            this.iosConf = iosConfigurationAndTargetRetriever.getIosProjectConfiguration(project)
            this.ant = ant
        }
    }

    void buildRelease(Project project, String target, String configuration) {
        logger.lifecycle( "\n\n\n=== Building target ${target}, configuration ${configuration}  ===")
        if (System.getenv()["SKIP_IOS_BUILDS"] != null) {
            logger.lifecycle ("********************* CAUTION !!!! *********************************")
            logger.lifecycle ("* Skipping iOS builds because SKIP_IOS_BUILDS variable is set  *")
            logger.lifecycle ("* This should never happen on actual jenkins build                 *")
            logger.lifecycle ("* If it does make sure that SKIP_IOS_BUILDS variable is unset    *")
            logger.lifecycle ("********************************************************************")
        } else {
            projectHelper.executeCommand(project, [
                "xcodebuild" ,
                "-target",
                target,
                "-configuration",
                configuration,
                "-sdk",
                iosConf.sdk
            ])
        }
        if (conf.versionString != null) {
            IOSArtifactBuilderInfo bi = buidSingleArtifactBuilderInfo(target, configuration, project)
            prepareDistributionZipFile(project, bi)
            prepareDSYMZipFile(project, bi)
            prepareIpaFile(project, bi)
            prepareManifestFile(project,bi)
            prepareMobileProvisionFile(project,bi)
        } else {
            logger.lifecycle("Skipping building artifacts -> the build is not versioned")
        }
    }

    private IOSArtifactBuilderInfo buidSingleArtifactBuilderInfo(String target, String configuration, Project project) {
        IOSArtifactBuilderInfo bi= new IOSArtifactBuilderInfo(
                id : "${target}-${configuration}",
                target : target,
                configuration : configuration,
                buildDirectory : new File(project.file( "build"),"${configuration}-iphoneos"),
                fullReleaseName : "${target}-${configuration}-${conf.fullVersionString}",
                folderPrefix : "${conf.projectDirectoryName}/${conf.fullVersionString}/${target}/${configuration}",
                filePrefix : "${target}-${configuration}-${conf.fullVersionString}",
                mobileprovisionFile : iosConfigurationAndTargetRetriever.findMobileProvisionFile(project, target, configuration),
                plistFile : iosConf.plistFile)
        return bi
    }

    private void prepareDistributionZipFile(Project project, IOSArtifactBuilderInfo bi) {
        AmebaArtifact distributionZipArtifact = prepareDistributionZipArtifact(bi)
        distributionZipArtifact.location.parentFile.mkdirs()
        distributionZipArtifact.location.delete()
        ant.zip(destfile: distributionZipArtifact.location) {
            zipfileset(dir: iosConf.distributionDirectory,
                    includes: iosConfigurationAndTargetRetriever.findMobileProvisionFile(project, bi.target, bi.configuration).name)
            zipfileset(dir: bi.buildDirectory , includes: "${bi.target}.app/**")
        }
        logger.lifecycle("Distribution zip file created: ${distributionZipArtifact}")
    }


    private void prepareDSYMZipFile(Project project, IOSArtifactBuilderInfo bi) {
        AmebaArtifact dSYMZipArtifact = prepareDSYMZipArtifact(bi)
        dSYMZipArtifact.location.parentFile.mkdirs()
        dSYMZipArtifact.location.delete()
        ant.zip(destfile: dSYMZipArtifact.location) {
            zipfileset(dir: bi.buildDirectory , includes: "${bi.target}.app.dSYM/**")
        }
        logger.lifecycle("dSYM zip file created: ${dSYMZipArtifact}")
    }


    private void prepareIpaFile(Project project, IOSArtifactBuilderInfo bi) {
        AmebaArtifact ipaArtifact = prepareIpaArtifact(bi)
        ipaArtifact.location.parentFile.mkdirs()
        ipaArtifact.location.delete()
        String[] command = [
            "/usr/bin/xcrun",
            "-sdk",
            iosConf.sdk,
            "PackageApplication",
            "-v",
            new File(bi.buildDirectory,"${bi.target}.app"),
            "-o",
            ipaArtifact.location,
            "--embed",
            bi.mobileprovisionFile
        ]
        projectHelper.executeCommand(project,command)
        logger.lifecycle("ipa file created: ${ipaArtifact}")
    }

    private void prepareManifestFile(Project project, IOSArtifactBuilderInfo bi) {
        AmebaArtifact manifestArtifact = prepareManifestArtifact(bi)
        manifestArtifact.location.parentFile.mkdirs()
        manifestArtifact.location.delete()

        URL manifestTemplate = this.class.getResource("manifest.plist")
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        def binding = [
                    ipaUrl : iosConf.ipaFiles.get(bi.id).url,
                    title : bi.target,
                    bundleId : MPParser.readBundleIdFromPlist(bi.plistFile.toURI().toURL())
                ]
        def result = engine.createTemplate(manifestTemplate).make(binding)
        manifestArtifact.location << (result.toString())
        logger.lifecycle("Manifest file created: ${manifestArtifact}")
    }

    private void prepareMobileProvisionFile(Project project, IOSArtifactBuilderInfo bi) {
        AmebaArtifact mobileProvisionArtifact = prepareMobileProvisionArtifact(bi)
        mobileProvisionArtifact.location.parentFile.mkdirs()
        mobileProvisionArtifact.location.delete()
        mobileProvisionArtifact.location << bi.mobileprovisionFile.text
        logger.lifecycle("Mobile provision file created: ${mobileProvisionArtifact}")
    }


    private AmebaArtifact prepareDistributionZipArtifact(IOSArtifactBuilderInfo bi, boolean checkIfExists = false) {
        AmebaArtifact distributionZipArtifact = new AmebaArtifact(
                name : "Distribution zip",
                url : new URL(conf.baseUrl, "${bi.folderPrefix}/${bi.filePrefix}.zip"),
                location : new File(conf.otaDirectory,"${bi.folderPrefix}/${bi.filePrefix}.zip"))
        if (!checkIfExists || distributionZipArtifact.location.exists()) {
            iosConf.distributionZipFiles.put(bi.id,distributionZipArtifact)
        } else {
            logger.lifecycle("Skipping preparing distribution zip for ${bi} -> missing")
        }
        return distributionZipArtifact
    }


    private AmebaArtifact prepareDSYMZipArtifact(IOSArtifactBuilderInfo bi, boolean checkIfExists = false) {
        AmebaArtifact dSYMZipArtifact = new AmebaArtifact(
                name : "dSYM zip",
                url : new URL(conf.baseUrl, "${bi.folderPrefix}/${bi.filePrefix}_dSYM.zip"),
                location : new File(conf.otaDirectory,"${bi.folderPrefix}/${bi.filePrefix}_dSYM.zip"))
        if (!checkIfExists || dSYMZipArtifact.location.exists() ) {
            iosConf.dSYMZipFiles.put(bi.id,dSYMZipArtifact)
        } else {
            logger.lifecycle("Skipping preparing dSYM artifact for ${bi.id} : ${dSYMZipArtifact.location} -> missing")
        }
        return dSYMZipArtifact
    }


    private AmebaArtifact prepareIpaArtifact(IOSArtifactBuilderInfo bi, boolean checkIfExists = false) {
        AmebaArtifact ipaArtifact = new AmebaArtifact(
                name : "The ipa file",
                url : new URL(conf.baseUrl, "${bi.folderPrefix}/${bi.filePrefix}.ipa"),
                location : new File(conf.otaDirectory,"${bi.folderPrefix}/${bi.filePrefix}.ipa"))
        if (!checkIfExists || ipaArtifact.location.exists()) {
            iosConf.ipaFiles.put(bi.id,ipaArtifact)
        } else {
            logger.lifecycle("Skipping preparing ipa artifact for ${bi.id} : ${ipaArtifact.location} -> missing")
        }
        return ipaArtifact
    }

    private AmebaArtifact prepareManifestArtifact(IOSArtifactBuilderInfo bi, boolean checkIfExists = false) {
        AmebaArtifact manifestArtifact = new AmebaArtifact(
                name : "The manifest file",
                url : new URL(conf.baseUrl, "${bi.folderPrefix}/manifest.plist"),
                location : new File(conf.otaDirectory,"${bi.folderPrefix}/manifest.plist"))
        if (!checkIfExists || manifestArtifact.location.exists()) {
            iosConf.manifestFiles.put(bi.id,manifestArtifact)
        } else {
            logger.lifecycle("Skipping preparing manifest artifact for ${bi.id} : ${manifestArtifact.location} -> missing")
        }
        return manifestArtifact
    }

    private AmebaArtifact prepareMobileProvisionArtifact(IOSArtifactBuilderInfo bi, boolean checkIfExists = false) {
        AmebaArtifact mobileProvisionArtifact = new AmebaArtifact(
                name : "The mobile provision file",
                url : new URL(conf.baseUrl, "${bi.folderPrefix}/${bi.filePrefix}.mobileprovision"),
                location : new File(conf.otaDirectory,"${bi.folderPrefix}/${bi.filePrefix}.mobileprovision"))
        if (!checkIfExists || mobileProvisionArtifact.location.exists()) {
            iosConf.mobileProvisionFiles.put(bi.id,mobileProvisionArtifact)
        } else {
            logger.lifecycle("Skipping preparing mobileProvision artifact for ${bi.id} : ${mobileProvisionArtifact.location} -> missing")
        }
        return mobileProvisionArtifact
    }


    void buildArtifactsOnly(Project project, String target, String configuration) {
        if (conf.versionString != null) {
            IOSArtifactBuilderInfo bi = buidSingleArtifactBuilderInfo(target, configuration, project)
            prepareDistributionZipArtifact(bi, true)
            prepareDSYMZipArtifact(bi, true)
            prepareIpaArtifact(bi, true)
            prepareManifestArtifact(bi, true)
            prepareMobileProvisionArtifact(bi, true)
        } else {
            logger.lifecycle("Skipping building artifacts -> the build is not versioned")
        }
    }
}
