package com.apphance.flow.plugins.ios.release

import com.apphance.flow.executor.command.Command
import com.apphance.flow.executor.jython.JythonExecutor
import com.apphance.flow.plugins.ios.builder.IOSArtifactProvider
import com.apphance.flow.plugins.ios.builder.IOSBuilderInfo
import com.apphance.flow.plugins.ios.parsers.PlistParser
import com.apphance.flow.plugins.release.FlowArtifact
import groovy.text.SimpleTemplateEngine
import groovy.transform.PackageScope

import javax.inject.Inject

import static org.gradle.api.logging.Logging.getLogger

class IOSDeviceArtifactsBuilder extends AbstractIOSArtifactsBuilder {

    private logger = getLogger(getClass())

    @Inject IOSArtifactProvider artifactProvider
    @Inject org.gradle.api.AntBuilder ant
    @Inject PlistParser plistParser

    void buildArtifacts(IOSBuilderInfo bi) {
        prepareDistributionZipFile(bi)
        prepareDSYMZipFile(bi)
        prepareAhSYMFiles(bi)
        prepareIpaFile(bi)
        prepareManifestFile(bi)
        prepareMobileProvisionFile(bi)
    }

    @PackageScope
    void prepareDistributionZipFile(IOSBuilderInfo bi) {
        def aa = artifactProvider.zipDistribution(bi)
        releaseConf.distributionZipFiles.put(bi.id, aa)
        aa.location.parentFile.mkdirs()
        aa.location.delete()
        ant.zip(destfile: aa.location) {
            zipfileset(dir: bi.mobileprovision.parent, includes: bi.mobileprovision)
            zipfileset(dir: bi.buildDir, includes: "${bi.buildableName}/**")
        }
        logger.info("Distribution zip file created: ${aa.location}")
    }

    @PackageScope
    void prepareDSYMZipFile(IOSBuilderInfo bi) {
        def aa = artifactProvider.dSYMZip(bi)
        releaseConf.dSYMZipFiles.put(bi.id, aa)
        aa.location.parentFile.mkdirs()
        aa.location.delete()
        ant.zip(destfile: aa.location) {
            zipfileset(dir: bi.buildDir, includes: "${bi.buildableName}.dSYM/**")
        }
        logger.info("dSYM zip file created: ${aa.location}")
    }

    @PackageScope
    void prepareAhSYMFiles(IOSBuilderInfo bi) {
        def aa = artifactProvider.ahSYM(bi)
        releaseConf.ahSYMDirs.put(bi.id, aa)
        aa.location.delete()
        aa.location.mkdirs()
        def je = new JythonExecutor()

        def dest = new File(bi.buildDir, "${bi.buildableName}.dSYM")
        def output = new File(aa.location.canonicalPath, bi.filePrefix)
        def args = ['-p', bi.plist.canonicalPath, '-d', dest.canonicalPath, '-o', output.canonicalPath]
        je.executeScript('dump_reduce3_flow.py', args)
        dest.listFiles().each {
            aa.childArtifacts << new FlowArtifact(name: it.name, location: it, url: "${aa.url.toString()}/${it.name}".toURL())
        }
        logger.info("ahSYM files created: ${aa.location}")
    }

    @PackageScope
    void prepareIpaFile(IOSBuilderInfo bi) {
        def aa = artifactProvider.ipa(bi)
        releaseConf.ipaFiles.put(bi.id, aa)
        aa.location.parentFile.mkdirs()
        aa.location.delete()
        def app = bi.buildDir.listFiles().find { it.name == bi.buildableName }
        def cmd = [
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
        ]
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: cmd))
        logger.info("IPA file created: ${aa.location}")
    }

    @PackageScope
    void prepareManifestFile(IOSBuilderInfo bi) {
        def aa = artifactProvider.manifest(bi)
        releaseConf.manifestFiles.put(bi.id, aa)
        aa.location.parentFile.mkdirs()
        aa.location.delete()

        def engine = new SimpleTemplateEngine()
        def bundleId = plistParser.evaluate(plistParser.bundleId(bi.plist), bi.target, bi.configuration)
        def binding = [
                ipaUrl: releaseConf.ipaFiles.get(bi.id).url,
                title: bi.target,
                bundleId: bundleId,
                versionString: bi.versionString
        ]
        logger.info("Building manifest from ${bi.plist}, bundleId: ${bundleId}")
        def result = engine.createTemplate(getClass().getResource('manifest.plist')).make(binding)
        aa.location << (result.toString())
        logger.info("Manifest file created: ${aa.location}")
    }

    @PackageScope
    void prepareMobileProvisionFile(IOSBuilderInfo bi) {
        def aa = artifactProvider.mobileprovision(bi)
        releaseConf.mobileProvisionFiles.put(bi.id, aa)
        aa.location.parentFile.mkdirs()
        aa.location.delete()
        aa.location << bi.mobileprovision.text
        logger.info("Mobile provision file created: ${aa.location}")
    }
}
