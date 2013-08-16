package com.apphance.flow.plugins.ios.release

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.IOSFamily
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.executor.command.CommandLogFilesGenerator
import com.apphance.flow.executor.linker.FileLinker
import com.apphance.flow.plugins.ios.builder.IOSBuilderInfo
import com.apphance.flow.plugins.ios.parsers.MobileProvisionParser
import spock.lang.Specification

import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.STD
import static com.google.common.io.Files.createTempDir
import static java.io.File.createTempFile

class IOSSimulatorArtifactsBuilderSpec extends Specification {

    def executor = new CommandExecutor(
            Mock(FileLinker) {
                fileLink(_) >> ''
            },
            Mock(CommandLogFilesGenerator) {
                commandLogFiles() >> [(STD): createTempFile('tmp', 'out'), (ERR): createTempFile('tmp', 'err')]
            }
    )
    def builder = new IOSSimulatorArtifactsBuilder(
            conf: GroovyMock(IOSConfiguration) {
                getRootDir() >> new File('.')
                getVersionCode() >> '3.1.45'
                getVersionString() >> '101'
            },
            executor: executor
    )

    def 'ios_sim_template is synced'() {
        given:
        def tmplDir = new File(getClass().getResource('ios_sim_tmpl').toURI())
        def tmpDir = createTempDir()

        when:
        builder.syncSimAppTemplateToTmpDir(tmplDir, tmpDir)

        then:
        def contents = new File(tmpDir, 'Contents')
        contents.exists()
        contents.isDirectory()
        folderSize(contents) < 410000

        and:
        def launcher = new File(contents, 'MacOS/Launcher')
        launcher.exists()
        launcher.isFile()
        launcher.canExecute()

        cleanup:
        tmpDir.deleteDir()
    }

    def 'source app is synced'() {
        given:
        def xcArchive = new File(getClass().getResource('GradleXCode.xcarchive').toURI())
        def srcApp = builder.sourceApp(GroovyMock(IOSBuilderInfo) {
            getArchiveDir() >> xcArchive
            getAppName() >> 'GradleXCode.app'
        })

        and:
        def tmpDir = createTempDir()
        tmpDir.deleteOnExit()

        expect:
        srcApp.canonicalPath.endsWith('GradleXCode.xcarchive/Products/Applications/GradleXCode.app')

        when:
        builder.syncAppToTmpDir(srcApp, tmpDir)

        then:
        def embedDir = new File(tmpDir, 'GradleXCode.app')
        embedDir.exists()
        embedDir.isDirectory()
        folderSize(embedDir) < 84000
    }

    def 'tmpDir is created with appDir inside'() {
        when:
        def tmpDir = builder.tmpDir(GroovyMock(IOSBuilderInfo) {
            getProductName() >> 'Some Application'
        }, family)

        then:
        tmpDir.isDirectory()
        tmpDir.name == "Some Application (${family.iFormat()}_Simulator) 101_3.1.45.app"

        where:
        family << IOSFamily.values()
    }

    long folderSize(File directory) {
        long length = 0
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length()
            else
                length += folderSize(file)
        }
        length
    }

    def 'bundleId is updated'() {
        given:
        builder.mpParser = GroovyMock(MobileProvisionParser)
        builder.executor = GroovyMock(CommandExecutor)

        when:
        builder.updateBundleId(Mock(File), Mock(File) {
            getAbsolutePath() >> 'absolute'
        })

        then:
        1 * builder.mpParser.bundleId(_) >> 'bid'
        1 * builder.executor.executeCommand({
            it.commandForExecution.join(' ') == '/usr/libexec/PlistBuddy -c Set :CFBundleIdentifier bid.launchsim absolute'
        })
    }

    def 'device family is updated'() {
        given:
        builder.executor = GroovyMock(CommandExecutor)

        when:
        builder.updateDeviceFamily(family, GroovyMock(File) {
            getAbsolutePath() >> 'absolute path'
        })

        then:
        1 * builder.executor.executeCommand({
            it.commandForExecution.join(' ') == '/usr/libexec/PlistBuddy -c Delete UIDeviceFamily absolute path'
        })
        1 * builder.executor.executeCommand({
            it.commandForExecution.join(' ') == '/usr/libexec/PlistBuddy -c Add UIDeviceFamily array absolute path'
        })
        1 * builder.executor.executeCommand({
            it.commandForExecution.join(' ') == "/usr/libexec/PlistBuddy -c Add UIDeviceFamily:0 integer $family.UIDDeviceFamily absolute path"
        })

        where:
        family << IOSFamily.values()
    }

    def 'icon is resampled'() {
        given:
        def iconFile = GroovyMock(File) {
            getCanonicalPath() >> 'canonical path 2'
        }
        builder.executor = GroovyMock(CommandExecutor)
        builder.releaseConf = GroovyMock(IOSReleaseConfiguration) {
            getIconFile() >> new FileProperty(value: iconFile)
        }

        when:
        builder.resampleIcon(GroovyMock(File) {
            getCanonicalPath() >> 'canonical path'
        })

        then:
        1 * builder.executor.executeCommand({
            def cfe = it.commandForExecution.join(' ')
            cfe.startsWith('/opt/local/bin/convert') && cfe.endsWith('-resample 128x128 canonical path')
        })
    }

    def 'app sim dmg is created'() {
        given:
        builder.executor = GroovyMock(CommandExecutor)

        when:
        builder.createSimAppDmg(GroovyMock(File) {
            getCanonicalPath() >> 'canonical path 1'
        }, GroovyMock(File) {
            getCanonicalPath() >> 'canonical path 2'
        }, 'name')

        then:
        1 * builder.executor.executeCommand({
            it.commandForExecution.join(' ') == 'hdiutil create canonical path 1 -srcfolder canonical path 2 -volname name'
        })
    }
}
