package com.apphance.ameba.plugins.ios.framework.tasks

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.ios.IOSFrameworkConfiguration
import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant
import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD
import static org.gradle.api.logging.Logging.getLogger

/**
 * Builds iOS framework.
 */
class BuildFrameworkTask extends DefaultTask {

    static String NAME = 'buildFramework'
    String group = AMEBA_BUILD
    String description = 'Builds iOS framework project'

    static final String FRAMEWORK_BUILD_PATH = 'Development-Framework'

    def l = getLogger(getClass())

    private File frameworkAppDir
    private File frameworkMainDir
    private File frameworkVersionsDir
    private File frameworkVersionsVersionDir
    private File frameworkVersionsVersionResourcesDir
    private File frameworkVersionsVersionHeadersDir
    private File iphoneosLibrary
    private File iphoneosSimulatorLibrary
    private File destinationZipFile

    @Inject
    CommandExecutor executor
    @Inject
    IOSConfiguration conf
    @Inject
    IOSFrameworkConfiguration frameworkConf

    AbstractIOSVariant variant

    @TaskAction
    void buildIOSFramework() {
        xcodeBuilds()
        cleanFrameworkDir()
        createDirectoryStructure()
        createSymlinks()
        setLinkLibraries()
        createLibrary()
        copyingResources()
        copyingHeaders()
        createZipFile()
    }

    private createZipFile() {
        destinationZipFile = new File(project.buildDir, variant.projectName + '_' + variant.versionString + '.zip')
        destinationZipFile.delete()
        executor.executeCommand(new Command(runDir: frameworkMainDir, cmd: [
                'zip',
                '-ry',
                destinationZipFile.canonicalPath,
                frameworkAppDir.name]))
    }

    private createLibrary() {
        l.lifecycle('Create library')
        def outputFile = new File(frameworkVersionsVersionDir, variant.projectName)
        outputFile.parentFile.mkdirs()
        executor.executeCommand(new Command(runDir: project.rootDir, cmd: [
                'lipo',
                '-create',
                iphoneosLibrary,
                iphoneosSimulatorLibrary,
                '-output',
                outputFile.canonicalPath]))
    }

    private copyingResources() {
        l.lifecycle('Copying resources')
        frameworkConf.resources.value.each {
            if (it != '') {
                project.ant.copy(file: it, toDir: frameworkVersionsVersionResourcesDir)
            }
        }
    }

    private copyingHeaders() {
        l.lifecycle('Copying headers')
        frameworkConf.headers.value.each {
            if (it != '') {
                project.ant.copy(file: it, toDir: frameworkVersionsVersionHeadersDir)
            }
        }
    }

    private setLinkLibraries() {
        l.lifecycle('Set link libraries')
        iphoneosLibrary = new File(project.buildDir, "${variant.configuration}-iphoneos/lib${variant.target}.a")
        iphoneosSimulatorLibrary = new File(project.buildDir, "${variant.configuration}-iphonesimulator/lib${variant.target}.a")
    }

    private createSymlinks() {
        l.lifecycle('Creating symlinks')
        executor.executeCommand(new Command(runDir: frameworkVersionsDir, cmd: [
                'ln',
                '-s',
                frameworkConf.version.value,
                'Current'
        ]))
        executor.executeCommand(new Command(runDir: frameworkAppDir, cmd: [
                'ln',
                '-s',
                'Versions/Current/Headers',
                'Headers'
        ]))
        executor.executeCommand(new Command(runDir: frameworkAppDir, cmd: [
                'ln',
                '-s',
                'Versions/Current/Resources',
                'Resources'
        ]))
        executor.executeCommand(new Command(runDir: frameworkAppDir, cmd: [
                'ln',
                '-s',
                "Versions/Current/${variant.projectName}",
                variant.projectName
        ]))
    }


    private cleanFrameworkDir() {
        frameworkMainDir = new File(project.buildDir, FRAMEWORK_BUILD_PATH)
        l.lifecycle("Cleaning framework dir: ${frameworkMainDir}")
        project.ant.delete(dir: frameworkMainDir)
    }

    private createDirectoryStructure() {
        l.lifecycle('Creating directory structure')
        frameworkMainDir.mkdirs()
        frameworkAppDir = new File(frameworkMainDir, "${variant.projectName}.framework")
        frameworkAppDir.mkdirs()
        frameworkVersionsDir = new File(frameworkAppDir, 'Versions')
        frameworkVersionsDir.mkdirs()
        frameworkVersionsVersionDir = new File(frameworkVersionsDir, frameworkConf.version.value)
        frameworkVersionsVersionDir.mkdirs()
        frameworkVersionsVersionResourcesDir = new File(frameworkVersionsVersionDir, 'Resources')
        frameworkVersionsVersionResourcesDir.mkdirs()
        frameworkVersionsVersionHeadersDir = new File(frameworkVersionsVersionDir, 'Headers')
        frameworkVersionsVersionHeadersDir.mkdirs()
    }

    private xcodeBuilds() {
        executor.executeCommand(new Command(runDir: project.rootDir, cmd: conf.xcodebuildExecutionPath() + [
                '-target',
                variant.target,
                '-configuration',
                variant.configuration,
                '-sdk',
                conf.simulatorSdk.value,
                '-arch',
                'i386',
                'clean',
                'build'
        ]))
        executor.executeCommand(new Command(runDir: project.rootDir, cmd: conf.xcodebuildExecutionPath() + [
                '-target',
                variant.target,
                '-configuration',
                variant.configuration,
                '-sdk',
                conf.sdk.value,
                'clean',
                'build'
        ]))
    }
}
