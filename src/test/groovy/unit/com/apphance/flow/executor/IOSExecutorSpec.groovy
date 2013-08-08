package com.apphance.flow.executor

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.executor.command.CommandLogFilesGenerator
import com.apphance.flow.executor.linker.FileLinker
import com.apphance.flow.plugins.ios.parsers.XCodeOutputParser
import groovy.json.JsonSlurper
import org.gradle.api.Project
import spock.lang.Specification

import static com.apphance.flow.configuration.ios.IOSConfiguration.PROJECT_PBXPROJ
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.STD
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

        iosExecutor.executor = executor
        iosExecutor.conf = conf
        iosExecutor.parser = new XCodeOutputParser()
    }

    def cleanup() {
        logFiles.each {
            it.value.delete()
        }
    }

    def 'pbxproj is converted to json format well'() {
        when:
        def json = iosExecutor.pbxProjToJSON(new File("$conf.rootDir.absolutePath/$conf.xcodeDir.value.name", PROJECT_PBXPROJ))

        then:
        noExceptionThrown()

        and:
        def slurped = new JsonSlurper().parseText(json.join('\n'))
        slurped.objectVersion == '46'
        slurped.archiveVersion == '1'
    }

    def 'plist is converted to json format well'() {
        when:
        def json = iosExecutor.plistToJSON(new File(getClass().getResource('Test.plist').toURI()))
        json = json.join('\n')

        then:
        noExceptionThrown()

        and:
        def slurped = new JsonSlurper().parseText(json)
        slurped.CFBundleName == 'Some'
        slurped.CFBundleIdentifier == 'com.apphance.flow'
    }

    def 'build settings got for target and configuration'() {
        when:
        def settings = iosExecutor.buildSettings('GradleXCode', 'BasicConfiguration')

        then:
        settings.size() > 0
        settings.keySet().every { it.matches('([A-Z0-9a-z]+_)*([A-Z0-9a-z])+') }
    }

    def 'runs dot clean'() {
        given:
        def ce = GroovyMock(CommandExecutor)

        and:
        def iose = new IOSExecutor(executor: ce, conf: GroovySpy(IOSConfiguration) {
            getRootDir() >> new File('sampleDir')
        })

        when:
        iose.clean()

        then:
        1 * ce.executeCommand({ it.commandForExecution.join(' ') == 'dot_clean ./' && it.runDir.name == 'sampleDir' })
    }

    def 'mobileprovision is converted to xml well'() {
        when:
        def xml = iosExecutor.mobileprovisionToXml(
                new File(conf.rootDir, 'release/distribution_resources/GradleXCode.mobileprovision'))

        then:
        xml.join('\n') == mobileprovisionXml
    }

    def 'xCode version is read correctly'() {
        expect:
        iosExecutor.xCodeVersion.matches('(\\d+\\.)+\\d+')
    }

    def 'ios-sim version is read correctly'() {
        expect:
        iosExecutor.iOSSimVersion.matches('(\\d+\\.)+\\d+')
    }

    def 'archive command is executed well'() {
        given:
        def ce = GroovyMock(CommandExecutor)

        and:
        def rootDir = new File('rootDir')

        and:
        def iose = new IOSExecutor(executor: ce, conf: GroovySpy(IOSConfiguration) {
            getRootDir() >> rootDir
        })

        when:
        iose.archiveVariant(rootDir, ['xcodebuild', '-project', 'Sample.xcodeproj', '-scheme', 's1', 'clean', 'archive'])

        then:
        1 * ce.executeCommand({ it.commandForExecution.join(' ') == 'xcodebuild -project Sample.xcodeproj -scheme s1 clean archive' && it.runDir.name == 'rootDir' })

        cleanup:
        rootDir.delete()
    }

    def 'running tests is executed well'() {
        given:
        def ce = GroovyMock(CommandExecutor)

        and:
        def rootDir = new File('rootDir')

        and:
        def iose = new IOSExecutor(executor: ce, conf: GroovySpy(IOSConfiguration) {
            getRootDir() >> rootDir
            xcodebuildExecutionPath() >> ['xcodebuild', '-project', 'Sample.xcodeproj']
        })

        when:
        iose.runTests(rootDir, 't1', 'c1', 'somePath')

        then:
        1 * ce.executeCommand(
                {
                    it.commandForExecution.join(' ') == 'xcodebuild -project Sample.xcodeproj -target t1 -configuration c1 -sdk iphonesimulator clean build' &&
                            it.runDir.name == 'rootDir' &&
                            it.failOnError == false &&
                            it.environment.RUN_UNIT_TEST_WITH_IOS_SIM == 'YES' &&
                            it.environment.UNIT_TEST_OUTPUT_FILE == 'somePath'
                })

        cleanup:
        rootDir.delete()
    }

    def 'dwarfdump arch is executed well'() {
        given:
        def ce = GroovyMock(CommandExecutor)

        and:
        def dSYM = new File('dSYM')

        and:
        def iose = new IOSExecutor(executor: ce)

        when:
        iose.dwarfdumpArch(dSYM, 'armv7')

        then:
        1 * ce.executeCommand({
            it.commandForExecution.join(' ') == "dwarfdump --arch armv7 ${dSYM.absolutePath}" &&
                    it.runDir == dSYM.parentFile
        })

        cleanup:
        dSYM.delete()
    }

    def 'dwarfdump UUID is executed well'() {
        given:
        def ce = GroovyMock(CommandExecutor)

        and:
        def dSYM = new File('dSYM')

        and:
        def iose = new IOSExecutor(executor: ce)

        when:
        iose.dwarfdumpUUID(dSYM)

        then:
        1 * ce.executeCommand({
            it.commandForExecution.join(' ') == "dwarfdump -u ${dSYM.absolutePath}" &&
                    it.runDir == dSYM.parentFile
        })

        cleanup:
        dSYM.delete()

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
