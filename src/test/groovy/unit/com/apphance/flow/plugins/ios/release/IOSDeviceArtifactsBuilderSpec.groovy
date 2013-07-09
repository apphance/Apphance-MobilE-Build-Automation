package com.apphance.flow.plugins.ios.release

import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.plugins.ios.builder.IOSArtifactProvider
import com.apphance.flow.plugins.ios.builder.IOSBuilderInfo
import com.apphance.flow.plugins.ios.parsers.PlistParser
import com.apphance.flow.plugins.release.FlowArtifact
import spock.lang.Specification

import static com.google.common.io.Files.createTempDir

class IOSDeviceArtifactsBuilderSpec extends Specification {

    def 'manifest is build and filled correctly'() {
        given:
        def tmpDir = createTempDir()

        and:
        def manifestFile = new File(tmpDir, 'manifest.plist')

        and:
        def deviceArtifactsBuilder = new IOSDeviceArtifactsBuilder(
                artifactProvider: GroovyMock(IOSArtifactProvider) {
                    manifest(_) >> new FlowArtifact(
                            name: 'Manifest file',
                            location: manifestFile
                    )
                },
                releaseConf: GroovyStub(IOSReleaseConfiguration) {
                    getIpaFiles() >> ['variantId': GroovyMock(FlowArtifact) {
                        getUrl() >> 'http://ota.flow.com'.toURL()
                    }]
                },
                plistParser: GroovyMock(PlistParser) {
                    bundleId(_) >> 'com.flow.bundleId'
                }
        )

        when:
        deviceArtifactsBuilder.prepareManifestFile(GroovyMock(IOSBuilderInfo) {
            getId() >> 'variantId'
            getTarget() >> 'sampleTarget'
            getVersionString() >> '3.1.45'
        })

        then:
        manifestFile.exists()
        manifestFile.size() > 0
        manifestFile.text.contains('<string>http://ota.flow.com</string>')
        manifestFile.text.contains('<string>com.flow.bundleId</string>')
        manifestFile.text.contains('<string>sampleTarget</string>')
        manifestFile.text.contains('<string>3.1.45</string>')

        cleanup:
        tmpDir.deleteDir()
    }
}
