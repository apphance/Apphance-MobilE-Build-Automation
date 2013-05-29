package com.apphance.ameba.plugins.ios.release

import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.jython.JythonExecutor
import com.apphance.ameba.plugins.ios.builder.IOSArtifactProvider
import com.apphance.ameba.plugins.ios.builder.IOSBuilderInfo
import com.apphance.ameba.plugins.ios.parsers.PlistParser
import com.apphance.ameba.plugins.release.AmebaArtifact
import groovy.text.SimpleTemplateEngine

import javax.inject.Inject

import static org.gradle.api.logging.Logging.getLogger

class IOSDeviceArtifactsBuilder extends AbstractIOSArtifactsBuilder {

    private l = getLogger(getClass())

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
            zipfileset(dir: bi.buildDir, includes: "${bi.target}.app/**")
        }
        l.lifecycle("Distribution zip file created: ${aa.location}")
    }

    private void prepareDSYMZipFile(IOSBuilderInfo bi) {
        def aa = artifactProvider.dSYMZip(bi)
        releaseConf.dSYMZipFiles.put(bi.id, aa)
        aa.location.parentFile.mkdirs()
        aa.location.delete()
        ant.zip(destfile: aa.location) {
            zipfileset(dir: bi.buildDir, includes: "${bi.target}.app.dSYM/**")
        }
        l.lifecycle("dSYM zip file created: ${aa.location}")
    }

    private void prepareAhSYMFiles(IOSBuilderInfo bi) {
        def aa = artifactProvider.ahSYM(bi)
        releaseConf.ahSYMDirs.put(bi.id, aa)
        aa.location.delete()
        aa.location.mkdirs()
        def je = new JythonExecutor()
        def dest = new File(bi.buildDir, "${bi.id}.app.dSYM")
        def output = new File(aa.location.canonicalPath, bi.filePrefix)
        def args = ['-p', bi.plist.canonicalPath, '-d', dest.canonicalPath, '-o', output.canonicalPath]
        je.executeScript('dump_reduce3_ameba.py', args)
        dest.listFiles().each {
            aa.childArtifacts << new AmebaArtifact(name: it.name, location: it, url: "${aa.url.toString()}/${it.name}".toURL())
        }
        l.lifecycle("ahSYM files created: ${aa.location}")
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
        l.lifecycle("ipa file created: ${aa.location}")
    }

    private void prepareManifestFile(IOSBuilderInfo bi) {
        def aa = artifactProvider.manifest(bi)
        releaseConf.manifestFiles.put(bi.id, aa)
        aa.location.parentFile.mkdirs()
        aa.location.delete()

        URL manifestTemplate = getClass().getResource('manifest.plist')
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        def bundleId = plistParser.evaluate(plistParser.bundleId(bi.plist), bi.target, bi.configuration)
        def binding = [
                ipaUrl: releaseConf.ipaFiles.get(bi.id).url,
                title: bi.target,
                bundleId: bundleId
        ]
        l.lifecycle("Building manifest from ${bi.plist}, bundleId: ${bundleId}")
        def result = engine.createTemplate(manifestTemplate).make(binding)
        aa.location << (result.toString())
        l.lifecycle("Manifest file created: ${aa.location}")
    }

    private void prepareMobileProvisionFile(IOSBuilderInfo bi) {
        def aa = artifactProvider.mobileprovision(bi)
        releaseConf.mobileProvisionFiles.put(bi.id, aa)
        aa.location.parentFile.mkdirs()
        aa.location.delete()
        aa.location << bi.mobileprovision.text
        l.lifecycle("Mobile provision file created: ${aa.location}")
    }
}
