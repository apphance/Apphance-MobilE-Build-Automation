package com.apphance.ameba.plugins.ios.framework

import com.apphance.ameba.plugins.projectconfiguration.ProjectConfiguration
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.ios.IOSProjectConfiguration
import com.apphance.ameba.plugins.ios.buildplugin.IOSPlugin
import org.gradle.api.Project

import static org.gradle.api.logging.Logging.getLogger

/**
 * Builds iOS framework.
 */
//TODO is this class/task really used?
class IOSFrameworkBuilder {

    static final String FRAMEWORK_BUILD_PATH = 'Development-Framework'

    def l = getLogger(getClass())

    private ProjectConfiguration conf
    private IOSProjectConfiguration iosConf

    private String frameworkTarget
    private String frameworkConfiguration
    private String frameworkVersion
    private List<String> frameworkHeaders
    private List<String> frameworkResources

    private File frameworkAppDir
    private File frameworkMainDir
    private File frameworkVersionsDir
    private File frameworkVersionsVersionDir
    private File frameworkVersionsVersionResourcesDir
    private File frameworkVersionsVersionHeadersDir
    private File iphoneosLibrary
    private File iphoneosSimulatorLibrary
    private File destinationZipFile
    private Project project
    private CommandExecutor executor

    IOSFrameworkBuilder(Project project, CommandExecutor executor) {
        this.project = project
        this.executor = executor
        use(PropertyCategory) {
            this.conf = project.getProjectConfiguration()
        }
    }

    void buildIOSFramework() {
        use(PropertyCategory) {
            iosConf = project.ext.get(IOSPlugin.IOS_PROJECT_CONFIGURATION)
            frameworkTarget = project.readExpectedProperty(IOSFrameworkProperty.FRAMEWORK_TARGET)
            frameworkConfiguration = project.readExpectedProperty(IOSFrameworkProperty.FRAMEWORK_CONFIGURATION)
            frameworkVersion = project.readExpectedProperty(IOSFrameworkProperty.FRAMEWORK_VERSION)
            frameworkHeaders = project.readExpectedProperty(IOSFrameworkProperty.FRAMEWORK_HEADERS).split(',')
            frameworkResources = project.readExpectedProperty(IOSFrameworkProperty.FRAMEWORK_RESOURCES).split(',')
        }
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
        destinationZipFile = new File(project.buildDir, conf.projectName + '_' + conf.versionString + '.zip')
        destinationZipFile.delete()
        executor.executeCommand(new Command(runDir: frameworkMainDir, cmd: [
                'zip',
                '-ry',
                destinationZipFile.canonicalPath,
                frameworkAppDir.name]))
    }

    private createLibrary() {
        l.lifecycle('Create library')
        def outputFile = new File(frameworkVersionsVersionDir, conf.projectName)
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
        frameworkResources.each {
            if (it != '') {
                project.ant.copy(file: it, toDir: frameworkVersionsVersionResourcesDir)
            }
        }
    }

    private copyingHeaders() {
        l.lifecycle('Copying headers')
        frameworkHeaders.each {
            if (it != '') {
                project.ant.copy(file: it, toDir: frameworkVersionsVersionHeadersDir)
            }
        }
    }

    private setLinkLibraries() {
        l.lifecycle('Set link libraries')
        iphoneosLibrary = new File(project.buildDir, "${frameworkConfiguration}-iphoneos/lib${frameworkTarget}.a")
        iphoneosSimulatorLibrary = new File(project.buildDir, "${frameworkConfiguration}-iphonesimulator/lib${frameworkTarget}.a")
    }

    private createSymlinks() {
        l.lifecycle('Creating symlinks')
        executor.executeCommand(new Command(runDir: frameworkVersionsDir, cmd: [
                'ln',
                '-s',
                frameworkVersion,
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
                "Versions/Current/${conf.projectName}",
                conf.projectName
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
        frameworkAppDir = new File(frameworkMainDir, "${conf.projectName}.framework")
        frameworkAppDir.mkdirs()
        frameworkVersionsDir = new File(frameworkAppDir, 'Versions')
        frameworkVersionsDir.mkdirs()
        frameworkVersionsVersionDir = new File(frameworkVersionsDir, frameworkVersion)
        frameworkVersionsVersionDir.mkdirs()
        frameworkVersionsVersionResourcesDir = new File(frameworkVersionsVersionDir, 'Resources')
        frameworkVersionsVersionResourcesDir.mkdirs()
        frameworkVersionsVersionHeadersDir = new File(frameworkVersionsVersionDir, 'Headers')
        frameworkVersionsVersionHeadersDir.mkdirs()
    }

    private xcodeBuilds() {
        executor.executeCommand(new Command(runDir: project.rootDir, cmd: iosConf.getXCodeBuildExecutionPath() + [
                '-target',
                frameworkTarget,
                '-configuration',
                frameworkConfiguration,
                '-sdk',
                iosConf.simulatorSDK,
                '-arch',
                'i386',
                'clean',
                'build'
        ]))
        executor.executeCommand(new Command(runDir: project.rootDir, cmd: iosConf.getXCodeBuildExecutionPath() + [
                '-target',
                frameworkTarget,
                '-configuration',
                frameworkConfiguration,
                '-sdk',
                iosConf.sdk,
                'clean',
                'build'
        ]))
    }
}
