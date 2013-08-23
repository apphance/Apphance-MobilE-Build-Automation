package com.apphance.flow.plugins.ios.release

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.executor.command.CommandLogFilesGenerator
import com.apphance.flow.executor.linker.FileLinker
import com.apphance.flow.plugins.ios.release.artifact.IOSArtifactProvider
import com.apphance.flow.plugins.ios.release.artifact.IOSArtifactInfo
import com.apphance.flow.plugins.ios.parsers.MobileProvisionParserSpec
import com.apphance.flow.plugins.ios.parsers.PlistParser
import com.apphance.flow.plugins.release.FlowArtifact
import spock.lang.Specification

import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.STD
import static com.google.common.io.Files.createTempDir
import static java.io.File.createTempFile
import static org.gradle.testfixtures.ProjectBuilder.builder

class IOSDeviceArtifactsBuilderSpec extends Specification {

    def builderInfo = GroovyMock(IOSArtifactInfo) {
        getId() >> 'variantId'
        getVersionString() >> '3.1.45'
        getAppName() >> 'GradleXCode.app'
        getProductName() >> 'SampleProduct'
        getMobileprovision() >> new File(MobileProvisionParserSpec.class.getResource('Test.mobileprovision').toURI())
        getArchiveDir() >> new File(getClass().getResource('GradleXCode.xcarchive').toURI())
        getFilePrefix() >> 'GradleXCode-3.1.45'
    }

    def conf = GroovyMock(IOSConfiguration) {
        getRootDir() >> new File('.')
        getSdk() >> new StringProperty(value: 'iphoneos')
    }

    def fileLinker = Mock(FileLinker) {
        fileLink(_) >> ''
    }
    def logFiles = [(STD): createTempFile('tmp', 'file-out'), (ERR): createTempFile('tmp', 'file-err')]
    def logFileGenerator = Mock(CommandLogFilesGenerator) {
        commandLogFiles() >> logFiles
    }
    def executor = new CommandExecutor(fileLinker, logFileGenerator)
    def iosExecutor = new IOSExecutor(executor: executor, conf: conf)

    def tmpDir = createTempDir()
    def manifest = new File(tmpDir, 'manifest.plist')
    def mobileprovision = new File(tmpDir, 'mobileprovision.mobileprovision')
    def ipa = new File(tmpDir, 'ipa.ipa')
    def ahSYM = new File(tmpDir, 'ahSYM')
    def dSYMzip = new File(tmpDir, 'dSYM.zip')
    def distZip = new File(tmpDir, 'dist.zip')
    def xcArchiveZip = new File(tmpDir, 'xcarchive.zip')

    def deviceArtifactsBuilder = new IOSDeviceArtifactsBuilder(
            artifactProvider: GroovyMock(IOSArtifactProvider) {
                manifest(_) >> new FlowArtifact(location: manifest)
                mobileprovision(_) >> new FlowArtifact(location: mobileprovision)
                ipa(_) >> new FlowArtifact(location: ipa)
                ahSYM(_) >> new FlowArtifact(location: ahSYM, url: 'http://ota.polidea.pl'.toURL())
                dSYMZip(_) >> new FlowArtifact(location: dSYMzip)
                zipDistribution(_) >> new FlowArtifact(location: distZip)
                xcArchive(_) >> new FlowArtifact(location: xcArchiveZip)
            },
            releaseConf: GroovyStub(IOSReleaseConfiguration) {
                getIpaFiles() >> ['variantId': GroovyMock(FlowArtifact) {
                    getUrl() >> 'http://ota.flow.com'.toURL()
                }]
            },
            plistParser: GroovyMock(PlistParser) {
                bundleId(_) >> 'com.flow.bundleId'
            },
            conf: conf,
            executor: executor,
            dumpReducer: new IOSDumpReducer(executor: iosExecutor, plistParser: new PlistParser(executor: iosExecutor)),
            ant: builder().build().ant
    )

    def cleanup() {
        tmpDir.deleteDir()
        logFiles.each {
            it.value.delete()
        }
    }

    def 'xcarchive zip created'() {
        when:
        deviceArtifactsBuilder.prepareXCArchiveZip(builderInfo)

        then:
        noExceptionThrown()
        xcArchiveZip.exists()
        xcArchiveZip.isFile()
        xcArchiveZip.size() > 159000
    }

    def 'distribution zip created'() {
        when:
        deviceArtifactsBuilder.prepareDistributionZipFile(builderInfo)

        then:
        noExceptionThrown()
        distZip.exists()
        distZip.isFile()
        distZip.size() > 32000
    }

    def 'dSYM zip created'() {
        when:
        deviceArtifactsBuilder.prepareDSYMZipFile(builderInfo)

        then:
        noExceptionThrown()
        dSYMzip.exists()
        dSYMzip.isFile()
        dSYMzip.size() > 123000
    }

    def 'ahSYM files created'() {
        when:
        deviceArtifactsBuilder.prepareAhSYMFiles(builderInfo)

        then:
        noExceptionThrown()
        ahSYM.exists()
        ahSYM.isDirectory()
        def filePrefix = 'GradleXCode-3.1.45'
        ['_armv7.ahsym', '_armv7s.ahsym'].every {
            def f = new File(ahSYM, "$filePrefix$it")
            f.exists() && f.isFile() && f.size() > 500
        }
    }

    def 'ipa file is created'() {
        when:
        deviceArtifactsBuilder.prepareIpaFile(builderInfo)

        then:
        noExceptionThrown()
        ipa.exists()
        ipa.size() > 30000
    }

    def 'manifest.plist file is created'() {
        when:
        deviceArtifactsBuilder.prepareManifestFile(builderInfo)

        then:
        noExceptionThrown()
        manifest.exists()
        manifest.size() > 0
        manifest.text.contains('<string>http://ota.flow.com</string>')
        manifest.text.contains('<string>com.flow.bundleId</string>')
        manifest.text.contains('<string>SampleProduct</string>')
        manifest.text.contains('<string>3.1.45</string>')
    }

    def 'mobileprovision file is created'() {
        when:
        deviceArtifactsBuilder.prepareMobileProvisionFile(builderInfo)

        then:
        noExceptionThrown()
        mobileprovision.exists()
        mobileprovision.size() > 0
        mobileprovision.text == builderInfo.mobileprovision.text
    }
}
