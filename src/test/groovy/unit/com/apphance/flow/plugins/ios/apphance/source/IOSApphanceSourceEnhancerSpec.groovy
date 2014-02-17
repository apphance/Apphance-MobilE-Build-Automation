package com.apphance.flow.plugins.ios.apphance.source

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.properties.ApphanceModeProperty
import com.apphance.flow.configuration.properties.BooleanProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.configuration.properties.URLProperty
import com.apphance.flow.plugins.ios.apphance.pbx.IOSApphancePbxEnhancer
import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Shared
import spock.lang.Specification

import static com.apphance.flow.configuration.apphance.ApphanceMode.QA
import static com.google.common.io.Files.copy
import static com.google.common.io.Files.createTempDir
import static java.util.regex.Pattern.compile

class IOSApphanceSourceEnhancerSpec extends Specification {

    @Shared
    def projectDir = new File('demo/ios/GradleXCode')

    def tmpDir = createTempDir()

    def cleanup() {
        tmpDir.deleteDir()
    }

    def 'NSLog is replaced with APHLog'() {
        given:
        def filesToReplace = [
                'GradleXCode/subdir/emptyPathView.m',
                'GradleXCode/gradleXCodeAppDelegate.m',
                'GradleXCode/gradleXCodeViewController.m',
                'GradleXCode/main.m',
                'GradleXCode/subdir/NonEmptyPathView.m'
        ]

        new AntBuilder().copy(toDir: tmpDir.absolutePath) {
            fileset(dir: projectDir.absolutePath) {
                filesToReplace.each {
                    include(name: it)
                    include(name: it.replace('.m', '.h'))
                }
            }
        }

        and:
        def sourceEnhancer = new IOSApphanceSourceEnhancer(
                GroovyMock(AbstractIOSVariant) {
                    getTmpDir() >> tmpDir
                },
                GroovyMock(IOSApphancePbxEnhancer) {
                    getFilesToReplaceLogs() >> filesToReplace
                },

        )
        sourceEnhancer.ant = ProjectBuilder.builder().build().ant

        when:
        sourceEnhancer.replaceLogs()

        then:
        def files = filesToReplace.collect { new File(tmpDir, it) }
        files.any { it.text.contains('APHLog') }
        !files.any {
            it.readLines().find { line ->
                def m = compile("\\bNSLog\\b").matcher(line)
                m.find()
            }
        }
        files.findAll { it.text.contains('NSLogPageSize') }.size() == 1
    }

    def 'apphance is added to PCH file'() {
        given:
        def sourceEnhancer = new IOSApphanceSourceEnhancer(
                GroovyMock(AbstractIOSVariant) {
                    getTmpDir() >> tmpDir
                    getAphMode() >> new ApphanceModeProperty(value: QA)
                },
                GroovyMock(IOSApphancePbxEnhancer) {
                    getGCCPrefixFilePaths() >> ['GradleXCode/GradleXCode-Prefix.pch']
                },
        )
        def tmpPch = new File(tmpDir, 'GradleXCode/GradleXCode-Prefix.pch')
        new File(tmpDir, 'GradleXCode').mkdirs()
        and:
        copy(new File(projectDir, 'GradleXCode/GradleXCode-Prefix.pch'), tmpPch)

        when:
        sourceEnhancer.addApphanceToPch()

        then:
        tmpPch.text.contains('#import <Apphance-Pre-Production/APHLogger.h>')
    }

    def 'exception while adding apphance to PCH file'() {
        given:
        def sourceEnhancer = new IOSApphanceSourceEnhancer(
                GroovyMock(AbstractIOSVariant) {
                    getTmpDir() >> tmpDir
                    getAphMode() >> new ApphanceModeProperty(value: QA)
                },
                GroovyMock(IOSApphancePbxEnhancer) {
                    getGCCPrefixFilePaths() >> ['GradleXCode/GradleXCode-Prefix.pch']
                },
        )

        def tmpPch = new File(tmpDir, 'GradleXCode/GradleXCode-Prefix.pch')
        new File(tmpDir, 'GradleXCode').mkdirs()
        and:
        copy(new File(projectDir, 'GradleXCode/GradleXCode-Prefix.pch'), tmpPch)

        and:
        tmpPch.text = tmpPch.text.replace('__OBJC__', "\n__OBJC__")

        when:
        sourceEnhancer.addApphanceToPch()

        then:
        def e = thrown(IllegalStateException)
        e.message == "Unable to add APHLogger to pch file: $tmpPch.absolutePath"
    }


    def 'apphance init section is added'() {
        given:
        def sourceEnhancer = new IOSApphanceSourceEnhancer(
                GroovyMock(AbstractIOSVariant) {
                    getTmpDir() >> tmpDir
                    getAphMode() >> new ApphanceModeProperty(value: QA)
                    getAphAppKey() >> '3145abcd'
                    getAphReportOnShake() >> new BooleanProperty(value: true)
                    getAphWithUTest() >> new BooleanProperty()
                    getAphWithScreenShotsFromGallery() >> new BooleanProperty()
                    getAphReportOnDoubleSlide() >> new BooleanProperty()
                    getAphMachException() >> new BooleanProperty()
                    getAphAppVersionCode() >> new StringProperty(value: '3145')
                    getAphAppVersionName() >> new StringProperty(value: '3.1.45')
                    getAphDefaultUser() >> new StringProperty()
                    getAphServerURL() >> new URLProperty(value: 'http://lol.com'.toURL())
                    getAphSendAllNSLogToApphance() >> new BooleanProperty()
                },
                null
        )

        and:
        copy(new File(getClass().getResource(hFile).toURI()), new File(tmpDir, mFile.replaceAll('.m', '.h')))
        copy(new File(getClass().getResource(mFile).toURI()), new File(tmpDir, mFile))

        when:
        sourceEnhancer.addApphanceInit()

        then:
        def mFileContent = new File(tmpDir, mFile).text
        mFileContent.contains('[APHLogger startNewSessionWithApplicationKey:@"3145abcd"')
        mFileContent.contains('NSSetUncaughtExceptionHandler(&APHUncaughtExceptionHandler);')

        where:
        hFile                      | mFile
        'gradleXCodeAppDelegate.h' | 'gradleXCodeAppDelegate.m'
        'gradleXCodeAppDelegate.h' | 'gradleXCodeAppDelegate2.m'
    }

    def 'exception is thrown when no UIApplicationDelegate file found'() {
        given:
        def sourceEnhancer = new IOSApphanceSourceEnhancer(
                GroovyMock(AbstractIOSVariant) {
                    getTmpDir() >> tmpDir
                    getAphMode() >> new ApphanceModeProperty(value: QA)
                    getAphAppKey() >> '3145abcd'
                },
                null
        )

        when:
        sourceEnhancer.addApphanceInit()

        then:
        def e = thrown(GradleException)
        e.message == "Can not find UIApplicationDelegate file in dir: $tmpDir.absolutePath"
    }

    def 'correct aph settings block is returned'() {
        given:
        def sourceEnhancer = new IOSApphanceSourceEnhancer(GroovyStub(AbstractIOSVariant) {
            getAphMode() >> new ApphanceModeProperty(value: QA)
            getAphReportOnShake() >> new BooleanProperty(value: true)
            getAphWithUTest() >> new BooleanProperty()
            getAphWithScreenShotsFromGallery() >> new BooleanProperty()
            getAphReportOnDoubleSlide() >> new BooleanProperty()
            getAphMachException() >> new BooleanProperty()
            getAphAppVersionCode() >> new StringProperty(value: '3145')
            getAphAppVersionName() >> new StringProperty(value: '3.1.45')
            getAphDefaultUser() >> new StringProperty()
            getAphSendAllNSLogToApphance() >> new BooleanProperty(value: false)
            getAphServerURL() >> new URLProperty(value: 'http://lol.com'.toURL())
        }, null)

        expect:
        sourceEnhancer.aphSettings ==
                '[[APHLogger defaultSettings] setReportOnShakeEnabled:YES];\n' +
                '[[APHLogger defaultSettings] setApplicationVersionCode:@"3145"];\n' +
                '[[APHLogger defaultSettings] setApplicationVersionName:@"3.1.45"];\n' +
                '[[APHLogger defaultSettings] setServerURL:@"http://lol.com"];\n' +
                '[[APHLogger defaultSettings] setSendAllNSLogToApphance:NO];'
    }

    def 'boolean property is mapped to APHSettings'() {
        given:
        def sourceEnhancer = new IOSApphanceSourceEnhancer(null, null)

        expect:
        expected == sourceEnhancer.mapBooleanPropToAPHSettings(new BooleanProperty(value: val), method)

        where:
        expected                               | val   | method
        ''                                     | null  | 'm'
        '[[APHLogger defaultSettings] m:NO];'  | false | 'm'
        '[[APHLogger defaultSettings] m:YES];' | true  | 'm'
    }

    def 'exception thrown when null property passed to mapBooleanPropToAPHSettings'() {
        given:
        def sourceEnhancer = new IOSApphanceSourceEnhancer(null, null)

        when:
        sourceEnhancer.mapBooleanPropToAPHSettings(null, null)

        then:
        def e = thrown(NullPointerException)
        e.message == 'Null property passed'
    }

    def 'exception thrown when empty method passed to mapBooleanPropToAPHSettings'() {
        given:
        def sourceEnhancer = new IOSApphanceSourceEnhancer(null, null)

        when:
        sourceEnhancer.mapBooleanPropToAPHSettings(new BooleanProperty(), method)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == 'Empty method passed'

        where:
        method << [null, '']
    }

    def 'string property is mapped to APHSettings'() {
        given:
        def sourceEnhancer = new IOSApphanceSourceEnhancer(null, null)

        expect:
        expected == sourceEnhancer.mapStringPropertyToAPHSettings(new StringProperty(value: val), method)

        where:
        expected                                 | val  | method
        ''                                       | null | 'm'
        ''                                       | ''   | 'm'
        '[[APHLogger defaultSettings] m:@"mm"];' | 'mm' | 'm'
    }
}
