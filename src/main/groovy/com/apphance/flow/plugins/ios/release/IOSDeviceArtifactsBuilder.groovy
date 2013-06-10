package com.apphance.flow.plugins.ios.release

import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.executor.command.Command
import com.apphance.flow.executor.jython.JythonExecutor
import com.apphance.flow.plugins.ios.builder.IOSArtifactProvider
import com.apphance.flow.plugins.ios.builder.IOSBuilderInfo
import com.apphance.flow.plugins.ios.parsers.PlistParser
import com.apphance.flow.plugins.release.FlowArtifact
import groovy.text.SimpleTemplateEngine

import javax.inject.Inject

import static org.gradle.api.logging.Logging.getLogger

class IOSDeviceArtifactsBuilder extends AbstractIOSArtifactsBuilder {

    private logger = getLogger(getClass())

    @Inject IOSArtifactProvider artifactProvider
    @Inject org.gradle.api.AntBuilder ant
    @Inject IOSExecutor iosExecutor
    @Inject PlistParser plistParser

    void buildArtifacts(IOSBuilderInfo bi) {
        prepareDistributionZipFile(bi)
        prepareDSYMZipFile(bi)
        prepareAhSYMFiles(bi)
        prepareIpaFile(bi)
        prepareManifestFile(bi)
        prepareMobileProvisionFile(bi)
    }

    private void prepareDistributionZipFile(IOSBuilderInfo bi) {
        def aa = artifactProvider.zipDistribution(bi)
        releaseConf.distributionZipFiles.put(bi.id, aa)
        aa.location.parentFile.mkdirs()
        aa.location.delete()
        ant.zip(destfile: aa.location) {
            zipfileset(dir: bi.mobileprovision.parent, includes: bi.mobileprovision)
            zipfileset(dir: bi.buildDir, includes: "${bi.buildableName}/**")
        }
        logger.lifecycle("Distribution zip file created: ${aa.location}")
    }

    private void prepareDSYMZipFile(IOSBuilderInfo bi) {
        def aa = artifactProvider.dSYMZip(bi)
        releaseConf.dSYMZipFiles.put(bi.id, aa)
        aa.location.parentFile.mkdirs()
        aa.location.delete()
        ant.zip(destfile: aa.location) {
            zipfileset(dir: bi.buildDir, includes: "${bi.buildableName}.dSYM/**")
        }
        logger.lifecycle("dSYM zip file created: ${aa.location}")
    }

    private void prepareAhSYMFiles(IOSBuilderInfo bi) {
        def aa = artifactProvider.ahSYM(bi)
        releaseConf.ahSYMDirs.put(bi.id, aa)
        aa.location.delete()
        aa.location.mkdirs()
        def je = new JythonExecutor()

        def dest = new File(bi.buildDir, "${bi.target}.app.dSYM")
        def output = new File(aa.location.canonicalPath, bi.filePrefix)
        def args = ['-p', bi.plist.canonicalPath, '-d', dest.canonicalPath, '-o', output.canonicalPath]
        je.executeScript('dump_reduce3_flow.py', args)
        dest.listFiles().each {
            aa.childArtifacts << new FlowArtifact(name: it.name, location: it, url: "${aa.url.toString()}/${it.name}".toURL())
        }
        logger.lifecycle("ahSYM files created: ${aa.location}")
    }

    private void prepareIpaFile(IOSBuilderInfo bi) {
        def aa = artifactProvider.ipa(bi)
        releaseConf.ipaFiles.put(bi.id, aa)
        aa.location.parentFile.mkdirs()
        aa.location.delete()
        def app = bi.buildDir.listFiles().find { it.name == iosExecutor.buildSettings(bi.target, bi.configuration)['FULL_PRODUCT_NAME'] }
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
        logger.lifecycle("ipa file created: ${aa.location}")
    }

    private void prepareManifestFile(IOSBuilderInfo bi) {
        def aa = artifactProvider.manifest(bi)
        releaseConf.manifestFiles.put(bi.id, aa)
        aa.location.parentFile.mkdirs()
        aa.location.delete()

        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        def bundleId = plistParser.evaluate(plistParser.bundleId(bi.plist), bi.target, bi.configuration)
        def binding = [
                ipaUrl: releaseConf.ipaFiles.get(bi.id).url,
                title: bi.target,
                bundleId: bundleId
        ]
        logger.lifecycle("Building manifest from ${bi.plist}, bundleId: ${bundleId}")
        def result = engine.createTemplate(getClass().getResource('manifest.plist')).make(binding)
        aa.location << (result.toString())
        logger.lifecycle("Manifest file created: ${aa.location}")
    }

    private void prepareMobileProvisionFile(IOSBuilderInfo bi) {
        def aa = artifactProvider.mobileprovision(bi)
        releaseConf.mobileProvisionFiles.put(bi.id, aa)
        aa.location.parentFile.mkdirs()
        aa.location.delete()
        aa.location << bi.mobileprovision.text
        logger.lifecycle("Mobile provision file created: ${aa.location}")
    }
}
