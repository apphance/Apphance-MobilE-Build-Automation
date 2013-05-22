package com.apphance.ameba.executor

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.executor.command.CommandLogFilesGenerator
import com.apphance.ameba.executor.linker.FileLinker
import groovy.json.JsonSlurper
import org.gradle.api.Project
import spock.lang.Specification

import static com.apphance.ameba.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.ameba.executor.command.CommandLogFilesGenerator.LogFile.STD
import static java.io.File.createTempFile

class IOSExecutorSpec extends Specification {

    def fileLinker = Mock(FileLinker)
    def logFileGenerator = Mock(CommandLogFilesGenerator)

    def executor = new CommandExecutor(fileLinker, logFileGenerator)

    def logFiles = [(STD): createTempFile('tmp', 'file-out'), (ERR): createTempFile('tmp', 'file-err')]

    def conf

    def iosExecutor = new IOSExecutor()

    def setup() {
        fileLinker.fileLink(_) >> ''
        logFileGenerator.commandLogFiles() >> logFiles

        conf = GroovySpy(IOSConfiguration)
        conf.project = GroovyStub(Project) {
            getRootDir() >> new File('testProjects/ios/GradleXCode')
        }
        conf.xcodeDir >> new FileProperty(value: new File('GradleXCode.xcodeproj'))

        iosExecutor.commandExecutor = executor
        iosExecutor.conf = conf
    }

    def cleanup() {
        logFiles.each {
            it.value.delete()
        }
    }

    def 'pbxproj is converted to xml format well'() {
        when:
        def xml = iosExecutor.pbxProjToXml()
        xml = xml.join('\n')

        then:
        noExceptionThrown()

        and:
        xml.startsWith('<?xml version="1.0" encoding="UTF-8"?>')

        and:
        def slurped = new XmlSlurper().parse(new ByteArrayInputStream(xml.bytes))
        slurped.dict.dict[1].key[0].text() == '6799F9CB151CA7A700178017'
    }

    def 'pbxproj is converted to json format well'() {
        when:
        def json = iosExecutor.pbxProjToJSON
        json = json.join('\n')

        then:
        noExceptionThrown()

        and:
        def slurped = new JsonSlurper().parseText(json)
        slurped.objectVersion == '46'
        slurped.archiveVersion == '1'
    }

    def 'plist is converted to json format well'() {
        when:
        def json = iosExecutor.plistToJSON(new File(conf.rootDir, 'GradleXCode/GradleXCode-Info.plist'))
        json = json.join('\n')

        then:
        noExceptionThrown()

        and:
        def slurped = new JsonSlurper().parseText(json)
        slurped.CFBundleName == '${PRODUCT_NAME}'
        slurped.CFBundleIdentifier == 'com.apphance.ameba'
    }

    def 'mobileprovision is converted to xml well'() {
        when:
        def xml = iosExecutor.mobileprovisionToXml(
                new File(conf.rootDir, 'release/distribution_resources/Ameba_Test_Project.mobileprovision'))

        then:
        xml.join('\n') == mobileprovisionXml
    }

    def mobileprovisionXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
            "<plist version=\"1.0\">\n" +
            "<dict>\n" +
            "\t<key>AppIDName</key>\n" +
            "\t<string>Ameba Test project</string>\n" +
            "\t<key>ApplicationIdentifierPrefix</key>\n" +
            "\t<array>\n" +
            "\t\t<string>MT2B94Q7N6</string>\n" +
            "\t</array>\n" +
            "\t<key>CreationDate</key>\n" +
            "\t<date>2013-03-20T12:08:10Z</date>\n" +
            "\t<key>DeveloperCertificates</key>\n" +
            "\t<array>\n" +
            "\t\t<data>\n" +
            "\t\tMIIFgzCCBGugAwIBAgIIeNHfa5jIf1UwDQYJKoZIhvcNAQEFBQAwgZYxCzAJ\n" +
            "\t\tBgNVBAYTAlVTMRMwEQYDVQQKDApBcHBsZSBJbmMuMSwwKgYDVQQLDCNBcHBs\n" +
            "\t\tZSBXb3JsZHdpZGUgRGV2ZWxvcGVyIFJlbGF0aW9uczFEMEIGA1UEAww7QXBw\n" +
            "\t\tbGUgV29ybGR3aWRlIERldmVsb3BlciBSZWxhdGlvbnMgQ2VydGlmaWNhdGlv\n" +
            "\t\tbiBBdXRob3JpdHkwHhcNMTIwNTA3MDgyMjQwWhcNMTMwNTA3MDgyMjQwWjB3\n" +
            "\t\tMRowGAYKCZImiZPyLGQBAQwKTVQyQjk0UTdONjElMCMGA1UEAwwcaVBob25l\n" +
            "\t\tIERpc3RyaWJ1dGlvbjogUG9saWRlYTETMBEGA1UECwwKTVQyQjk0UTdONjEQ\n" +
            "\t\tMA4GA1UECgwHUG9saWRlYTELMAkGA1UEBhMCUEwwggEiMA0GCSqGSIb3DQEB\n" +
            "\t\tAQUAA4IBDwAwggEKAoIBAQDiB1bwXcrKEm3oMiRqsN7GUBcdRFEbxZIfan/y\n" +
            "\t\t2teV1pqUr2DRWvE3SKduSx0MeGhZNPo1zd2kBd7McjdXCl8bDXLv3I3fb2kN\n" +
            "\t\tfF5edacdjJN3Yy5w6gBarTx/Z7Vac3u885T4PtUhR506vxBw9gj1xtfUlFRl\n" +
            "\t\tc10cRl0f8pAovsc4BxUa+VeJy0BDTDva4zVIJjlQC9cfRb+x5zbAmDr2WDRV\n" +
            "\t\t5YFsgWyxAMALw3C4wJYeiPQF+qOSf9WT1ruljWWe1xBRMR1URPBeSzgjMcR0\n" +
            "\t\tMpHceqU2e6F8DXsUi2yfRDlo6aIN4XWYhvSLIAn13ElvGNboH7JMQLnQp80J\n" +
            "\t\tABJXAgMBAAGjggHxMIIB7TAdBgNVHQ4EFgQUFLN0CC1/4ECehv98r5FcVpkC\n" +
            "\t\tsYIwDAYDVR0TAQH/BAIwADAfBgNVHSMEGDAWgBSIJxcJqbYYYIvs67r2R1nF\n" +
            "\t\tUlSjtzCCAQ8GA1UdIASCAQYwggECMIH/BgkqhkiG92NkBQEwgfEwgcMGCCsG\n" +
            "\t\tAQUFBwICMIG2DIGzUmVsaWFuY2Ugb24gdGhpcyBjZXJ0aWZpY2F0ZSBieSBh\n" +
            "\t\tbnkgcGFydHkgYXNzdW1lcyBhY2NlcHRhbmNlIG9mIHRoZSB0aGVuIGFwcGxp\n" +
            "\t\tY2FibGUgc3RhbmRhcmQgdGVybXMgYW5kIGNvbmRpdGlvbnMgb2YgdXNlLCBj\n" +
            "\t\tZXJ0aWZpY2F0ZSBwb2xpY3kgYW5kIGNlcnRpZmljYXRpb24gcHJhY3RpY2Ug\n" +
            "\t\tc3RhdGVtZW50cy4wKQYIKwYBBQUHAgEWHWh0dHA6Ly93d3cuYXBwbGUuY29t\n" +
            "\t\tL2FwcGxlY2EvME0GA1UdHwRGMEQwQqBAoD6GPGh0dHA6Ly9kZXZlbG9wZXIu\n" +
            "\t\tYXBwbGUuY29tL2NlcnRpZmljYXRpb25hdXRob3JpdHkvd3dkcmNhLmNybDAO\n" +
            "\t\tBgNVHQ8BAf8EBAMCB4AwFgYDVR0lAQH/BAwwCgYIKwYBBQUHAwMwEwYKKoZI\n" +
            "\t\thvdjZAYBBAEB/wQCBQAwDQYJKoZIhvcNAQEFBQADggEBAEwZ7eyBT6cZ3Eef\n" +
            "\t\trLCy9kd2sq3SzQaeE7WHPFb5rIkOq95gyYM6krwbMMPyDw3Yvr6LbwOlAvcg\n" +
            "\t\tztI1z+Avnakj1pWWOaEiu5eAlzPiQ0Egn/iom/hkCTu7yJq520CRJznLYGqR\n" +
            "\t\t4WDo08eeKgXKSPYeOTsINBuIEZ5y6ujV+GmkwgE+JrGV+A1rGbgDBcgRHA4z\n" +
            "\t\tJKrtjSEl7wSjIvA5HSyRtqmFCd4XpO61vvzIyCEQt3Mpsd+Ufl5YtQ4vOgZn\n" +
            "\t\t/ykdXUgYWQvwR+5LyDJvAkM8xPy6QphJWRfrp4YZGjYAPN2bDRTdJdHYmdJQ\n" +
            "\t\t6bcyzf5I7LI08VAzC0riAd+bQMg=\n" +
            "\t\t</data>\n" +
            "\t</array>\n" +
            "\t<key>Entitlements</key>\n" +
            "\t<dict>\n" +
            "\t\t<key>application-identifier</key>\n" +
            "\t\t<string>MT2B94Q7N6.com.apphance.ameba</string>\n" +
            "\t\t<key>get-task-allow</key>\n" +
            "\t\t<false/>\n" +
            "\t\t<key>keychain-access-groups</key>\n" +
            "\t\t<array>\n" +
            "\t\t\t<string>MT2B94Q7N6.*</string>\n" +
            "\t\t</array>\n" +
            "\t</dict>\n" +
            "\t<key>ExpirationDate</key>\n" +
            "\t<date>2013-05-06T12:08:10Z</date>\n" +
            "\t<key>Name</key>\n" +
            "\t<string>Ameba Test Project Ad Hoc</string>\n" +
            "\t<key>ProvisionedDevices</key>\n" +
            "\t<array>\n" +
            "\t\t<string>c66f6b79a8473d858a73908ee08c41a0a9400a64</string>\n" +
            "\t\t<string>4860904159146be176cfea2f4ddd3beffb38806d</string>\n" +
            "\t\t<string>e202fda7d6ab419d847d9fbf6d834ac7f2b7964f</string>\n" +
            "\t\t<string>e5de7d3f10023ea395f454cdacaac294997617f0</string>\n" +
            "\t\t<string>a397a1b037b6420e74add4f774de4db33410ed38</string>\n" +
            "\t\t<string>98abe71145419bce57254616cdddeefca0d70e86</string>\n" +
            "\t\t<string>cba04d41c62bcb845193a6942b13d7876e7849e0</string>\n" +
            "\t\t<string>26049eaba5c9af32ac3ac0b1475c69cb971db512</string>\n" +
            "\t\t<string>e099358191aef77651995c44e9c01e0a9c138782</string>\n" +
            "\t\t<string>ae0aadfb8d7f60cfcc40f4b63da96120f1cb2465</string>\n" +
            "\t\t<string>074b4502dcf5af019a5b1f7e3eb304129314d5d7</string>\n" +
            "\t\t<string>bea8af0efd1674d7efe2a82ec9e66aab74eee6ea</string>\n" +
            "\t\t<string>6d9665fdc0a916e184ad346846fe97d67ab64088</string>\n" +
            "\t\t<string>139fbb263b2d9d78417b90c71dbee6aa97f04a0f</string>\n" +
            "\t\t<string>bf92928a1ec68f7e153751c38946f02c72ab6bd6</string>\n" +
            "\t\t<string>d236d087781bf2d33426e227ceb1bbf84d60f862</string>\n" +
            "\t\t<string>249e3d99dd3b918bdcafda9280fcba4a5c7e7281</string>\n" +
            "\t\t<string>4b75d1db0b8ce04fde8a7ccd0d576f11f4c72415</string>\n" +
            "\t\t<string>18250ed94639fb94517f9cd55c039308a9b5d2ad</string>\n" +
            "\t</array>\n" +
            "\t<key>TeamIdentifier</key>\n" +
            "\t<array>\n" +
            "\t\t<string>MT2B94Q7N6</string>\n" +
            "\t</array>\n" +
            "\t<key>TeamName</key>\n" +
            "\t<string>Polidea</string>\n" +
            "\t<key>TimeToLive</key>\n" +
            "\t<integer>47</integer>\n" +
            "\t<key>UUID</key>\n" +
            "\t<string>73E1A3AB-D882-4CD1-B9C1-FC1EB72E1F1E</string>\n" +
            "\t<key>Version</key>\n" +
            "\t<integer>1</integer>\n" +
            "</dict>\n" +
            "</plist>"
}
