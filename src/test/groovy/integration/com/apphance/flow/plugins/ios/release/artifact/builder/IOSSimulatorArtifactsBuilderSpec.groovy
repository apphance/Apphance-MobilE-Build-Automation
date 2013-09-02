package com.apphance.flow.plugins.ios.release.artifact.builder

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.IOSFamily
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.executor.command.CommandLogFilesGenerator
import com.apphance.flow.executor.linker.FileLinker
import com.apphance.flow.plugins.ios.parsers.MobileProvisionParser
import com.apphance.flow.plugins.ios.release.artifact.info.IOSSimArtifactInfo
import com.apphance.flow.util.FlowUtils
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Paths

import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.STD
import static com.google.common.io.Files.createTempDir
import static java.io.File.createTempFile
import static java.nio.file.Files.isSymbolicLink

@Mixin(FlowUtils)
class IOSSimulatorArtifactsBuilderSpec extends Specification {

    @Shared
    def executor = new CommandExecutor(
            Mock(FileLinker) {
                fileLink(_) >> ''
            },
            Mock(CommandLogFilesGenerator) {
                commandLogFiles() >> [(STD): createTempFile('tmp', 'out'), (ERR): createTempFile('tmp', 'err')]
            }
    )
    @Shared
    def builder = new IOSSimulatorArtifactsBuilder(
            conf: GroovyMock(IOSConfiguration) {
                getRootDir() >> new File('.')
                getFullVersionString() >> '3.1.45_101'
            },
            releaseConf: GroovyMock(IOSReleaseConfiguration) {
                getReleaseIcon() >> new FileProperty(value: 'testProjects/ios/GradleXCode/icon.png')
            },
            executor: executor
    )

    def 'ios_sim_template is synced'() {
        given:
        def tmpDir = createTempDir()

        when:
        builder.syncSimAppTemplateToTmpDir(builder.tmplDir, tmpDir)

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

        and:
        def plSimulator = new File(contents, 'Frameworks/PLSimulator.framework/PLSimulator')
        isSymbolicLink(Paths.get(plSimulator.toURI()))

        cleanup:
        tmpDir.deleteDir()
    }

    def 'source app is synced'() {
        given:
        def xcArchive = new File(getClass().getResource('GradleXCode.xcarchive').toURI())
        def srcApp = builder.sourceApp(new IOSSimArtifactInfo(archiveDir: xcArchive, appName: 'GradleXCode.app'))
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
        def tmpDir = builder.tmpDir(new IOSSimArtifactInfo(productName: 'Some Application'), family)

        then:
        tmpDir.isDirectory()
        tmpDir.name == "Some Application (${family.iFormat()}_Simulator) 3.1.45_101.app"

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

    def 'icon is resized'() {
        given:
        def resizedIcon = new File(tempDir, 'icon-resized.png')

        when:
        builder.resampleIcon(resizedIcon)

        then:
        resizedIcon.exists()
        resizedIcon.size() > 4500
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
