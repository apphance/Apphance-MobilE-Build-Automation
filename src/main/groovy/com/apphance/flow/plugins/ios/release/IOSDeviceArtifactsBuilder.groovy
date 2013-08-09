package com.apphance.flow.plugins.ios.release

import com.apphance.flow.executor.command.Command
import com.apphance.flow.plugins.ios.builder.IOSBuilderInfo
import com.apphance.flow.plugins.ios.parsers.PlistParser
import com.apphance.flow.plugins.release.FlowArtifact
import groovy.text.SimpleTemplateEngine
import groovy.transform.PackageScope

import javax.inject.Inject

import static com.google.common.io.Files.createTempDir
import static org.gradle.api.logging.Logging.getLogger

class IOSDeviceArtifactsBuilder extends AbstractIOSArtifactsBuilder {

    private logger = getLogger(getClass())

    @Inject org.gradle.api.AntBuilder ant
    @Inject PlistParser plistParser
    @Inject IOSDumpReducer dumpReducer

    @Override
    void buildArtifacts(IOSBuilderInfo bi) {
        prepareXCArchiveZip(bi)
        prepareDistributionZipFile(bi)
        prepareDSYMZipFile(bi)
        prepareAhSYMFiles(bi)
        prepareIpaFile(bi)
        prepareManifestFile(bi)
        prepareMobileProvisionFile(bi)
    }

    @PackageScope
    void prepareXCArchiveZip(IOSBuilderInfo bi) {
        def aa = artifactProvider.xcArchive(bi)
        releaseConf.xcArchiveZipFiles.put(bi.id, aa)
        mkdirs(aa)
        def tmpDir = createTempDir()
        def appTmpDir = new File(tmpDir, "${bi.productName}.xcarchive")
        ant.sync(toDir: appTmpDir) {
            fileset(dir: bi.archiveDir)
        }
        ant.zip(destfile: aa.location) {
            zipfileset(dir: tmpDir)
        }
        tmpDir.deleteDir()
        logger.info("XC archive zip file created: $aa.location")
    }

    @PackageScope
    void prepareDistributionZipFile(IOSBuilderInfo bi) {
        def aa = artifactProvider.zipDistribution(bi)
        releaseConf.distributionZipFiles.put(bi.id, aa)
        mkdirs(aa)
        ant.zip(destfile: aa.location) {
            zipfileset(dir: bi.mobileprovision.parent, includes: bi.mobileprovision)
            zipfileset(dir: "$bi.archiveDir/Products/Applications", includes: "${bi.appName}/**")
        }
        logger.info("Distribution zip file created: $aa.location")
    }

    @PackageScope
    void prepareDSYMZipFile(IOSBuilderInfo bi) {
        def aa = artifactProvider.dSYMZip(bi)
        releaseConf.dSYMZipFiles.put(bi.id, aa)
        mkdirs(aa)
        ant.zip(destfile: aa.location) {
            zipfileset(dir: "$bi.archiveDir/dSYMs", includes: "${bi.appName}.dSYM/**")
        }
        logger.info("dSYM zip file created: $aa.location")
    }

    @PackageScope
    void prepareAhSYMFiles(IOSBuilderInfo bi) {
        def aa = artifactProvider.ahSYM(bi)
        releaseConf.ahSYMDirs.put(bi.id, aa)
        aa.location.delete()
        aa.location.mkdirs()

        def dSYM = new File("$bi.archiveDir/dSYMs", "${bi.appName}.dSYM")
        dumpReducer.reduce(plist.call(bi), dSYM, aa.location, bi.filePrefix)

        dSYM.listFiles().each {
            aa.childArtifacts << new FlowArtifact(name: it.name, location: it, url: "${aa.url.toString()}/${it.name}".toURL())
        }
        logger.info("ahSYM files created: $aa.location")
    }

    @PackageScope
    void prepareIpaFile(IOSBuilderInfo bi) {
        def aa = artifactProvider.ipa(bi)
        releaseConf.ipaFiles.put(bi.id, aa)
        mkdirs(aa)
        def app = new File("$bi.archiveDir/Products/Applications", bi.appName)
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: [
                '/usr/bin/xcrun',
                '-sdk',
                conf.sdk.value,
                'PackageApplication',
                '-v',
                app.canonicalPath,
                '-o',
                aa.location.canonicalPath,
                '--embed',
                bi.mobileprovision.canonicalPath
        ]))
        logger.info("IPA file created: $aa.location")
    }

    @PackageScope
    void prepareManifestFile(IOSBuilderInfo bi) {
        def aa = artifactProvider.manifest(bi)
        releaseConf.manifestFiles.put(bi.id, aa)
        mkdirs(aa)

        def engine = new SimpleTemplateEngine()
        def bundleId = plistParser.bundleId(plist.call(bi))
        def binding = [
                ipaUrl: releaseConf.ipaFiles.get(bi.id).url,
                title: bi.productName,
                bundleId: bundleId,
                versionString: bi.versionString
        ]
        logger.info("Building manifest from ${plist.call(bi).absolutePath}, bundleId: $bundleId")
        def result = engine.createTemplate(getClass().getResource('manifest.plist')).make(binding)
        aa.location << (result.toString())
        logger.info("Manifest file created: $aa.location")
    }

    @PackageScope
    void prepareMobileProvisionFile(IOSBuilderInfo bi) {
        def aa = artifactProvider.mobileprovision(bi)
        releaseConf.mobileProvisionFiles.put(bi.id, aa)
        mkdirs(aa)
        aa.location << bi.mobileprovision.text
        logger.info("Mobile provision file created: $aa.location")
    }

    @Lazy
    private Closure<File> plist = { IOSBuilderInfo bi ->
        new File("$bi.archiveDir/Products/Applications/$bi.appName", 'Info.plist')
    }.memoize()
}
