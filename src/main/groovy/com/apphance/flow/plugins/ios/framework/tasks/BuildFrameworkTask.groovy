package com.apphance.flow.plugins.ios.framework.tasks

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.IOSFrameworkConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.executor.command.Command
import com.apphance.flow.executor.command.CommandExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD

/**
 * Builds iOS framework.
 */
class BuildFrameworkTask extends DefaultTask {

    static String NAME = 'buildFramework'
    String group = FLOW_BUILD
    String description = 'Builds iOS framework project'

    static final String FRAMEWORK_BUILD_PATH = 'Development-Framework'

    private File frameworkAppDir
    private File frameworkMainDir
    private File frameworkVersionsDir
    private File frameworkVersionsVersionDir
    private File frameworkVersionsVersionResourcesDir
    private File frameworkVersionsVersionHeadersDir
    private File iphoneosLibrary
    private File iphoneosSimulatorLibrary
    private File destinationZipFile

    @Inject CommandExecutor executor
    @Inject IOSConfiguration conf
    @Inject IOSFrameworkConfiguration frameworkConf

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
        logger.lifecycle('Create library')
        def outputFile = new File(frameworkVersionsVersionDir, variant.projectName)
        outputFile.parentFile.mkdirs()
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: [
                'lipo',
                '-create',
                iphoneosLibrary,
                iphoneosSimulatorLibrary,
                '-output',
                outputFile.canonicalPath]))
    }

    private copyingResources() {
        logger.lifecycle('Copying resources')
        frameworkConf.resources.value.each {
            if (it != '') {
                project.ant.copy(file: it, toDir: frameworkVersionsVersionResourcesDir)
            }
        }
    }

    private copyingHeaders() {
        logger.lifecycle('Copying headers')
        frameworkConf.headers.value.each {
            if (it != '') {
                project.ant.copy(file: it, toDir: frameworkVersionsVersionHeadersDir)
            }
        }
    }

    private setLinkLibraries() {
        logger.lifecycle('Set link libraries')
        iphoneosLibrary = new File(project.buildDir, "${variant.configuration}-iphoneos/lib${variant.target}.a")
        iphoneosSimulatorLibrary = new File(project.buildDir, "${variant.configuration}-iphonesimulator/lib${variant.target}.a")
    }

    private createSymlinks() {
        logger.lifecycle('Creating symlinks')
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
        logger.lifecycle("Cleaning framework dir: ${frameworkMainDir}")
        project.ant.delete(dir: frameworkMainDir)
    }

    private createDirectoryStructure() {
        logger.lifecycle('Creating directory structure')
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
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: conf.xcodebuildExecutionPath() + [
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
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: conf.xcodebuildExecutionPath() + [
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
