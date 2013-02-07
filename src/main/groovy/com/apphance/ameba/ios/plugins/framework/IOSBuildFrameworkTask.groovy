package com.apphance.ameba.ios.plugins.framework

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.IOSXCodeOutputParser
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

/**
 * Builds iOS framework.
 */
class IOSBuildFrameworkTask extends DefaultTask {

    static final String FRAMEWORK_BUILD_PATH = 'Development-Framework'

    Logger logger = Logging.getLogger(IOSBuildFrameworkTask.class)
    ProjectHelper projectHelper
    ProjectConfiguration conf
    IOSProjectConfiguration iosConf

    String frameworkTarget
    String frameworkConfiguration
    String frameworkVersion
    List<String> frameworkHeaders
    List<String> frameworkResources

    File frameworkAppDir
    File frameworkMainDir
    File frameworkVersionsDir
    File frameworkVersionsVersionDir
    File frameworkVersionsVersionResourcesDir
    File frameworkVersionsVersionHeadersDir
    File iphoneosLibrary
    File iphonesimulatorLibrary
    File destinationZipFile

    IOSBuildFrameworkTask() {
        use(PropertyCategory) {
            this.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
            this.description = 'Builds iOS framework project'
            this.projectHelper = new ProjectHelper();
            this.conf = project.getProjectConfiguration()
            this.dependsOn(project.readProjectConfiguration)
            this.dependsOn(project.copyMobileProvision)
        }
    }

    @TaskAction
    void buildIOSFramework() {
        use(PropertyCategory) {
            iosConf = IOSXCodeOutputParser.getIosProjectConfiguration(project)
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
        projectHelper.executeCommand(project, this.frameworkMainDir, [
                "zip",
                "-ry",
                destinationZipFile,
                frameworkAppDir.name
        ])
    }

    private createLibrary() {
        logger.lifecycle("Create library")
        def outputFile = new File(this.frameworkVersionsVersionDir, conf.projectName)
        outputFile.parentFile.mkdirs()
        projectHelper.executeCommand(project,
                [
                        "lipo",
                        "-create",
                        this.iphoneosLibrary,
                        this.iphonesimulatorLibrary,
                        "-output",
                        outputFile]
        )
    }

    private copyingResources() {
        logger.lifecycle("Copying resources")
        frameworkResources.each {
            if (it != '') {
                project.ant.copy(file: it, toDir: this.frameworkVersionsVersionResourcesDir)
            }
        }
    }

    private copyingHeaders() {
        logger.lifecycle("Copying headers")
        frameworkHeaders.each {
            if (it != '') {
                project.ant.copy(file: it, toDir: this.frameworkVersionsVersionHeadersDir)
            }
        }
    }

    private setLinkLibraries() {
        logger.lifecycle("Set link libraries")
        this.iphoneosLibrary = new File(project.buildDir, frameworkConfiguration + '-' +
                'iphoneos/lib' + frameworkTarget + '.a')
        this.iphonesimulatorLibrary = new File(project.buildDir, frameworkConfiguration + '-' +
                'iphonesimulator/lib' + frameworkTarget + '.a')
    }

    private createSymlinks() {
        logger.lifecycle("Creating symlinks")
        projectHelper.executeCommand(project, frameworkVersionsDir, [
                "ln",
                "-s",
                frameworkVersion,
                "Current"
        ])
        projectHelper.executeCommand(project, frameworkAppDir, [
                "ln",
                "-s",
                "Versions/Current/Headers",
                "Headers"
        ])
        projectHelper.executeCommand(project, frameworkAppDir, [
                "ln",
                "-s",
                "Versions/Current/Resources",
                "Resources"
        ])
        projectHelper.executeCommand(project, frameworkAppDir, [
                "ln",
                "-s",
                "Versions/Current/${conf.projectName}",
                conf.projectName
        ])
    }


    private cleanFrameworkDir() {
        this.frameworkMainDir = new File(project.buildDir, FRAMEWORK_BUILD_PATH)
        logger.lifecycle("Cleaning framework dir: ${this.frameworkMainDir}")
        project.ant.delete(dir: frameworkMainDir)
    }

    private createDirectoryStructure() {
        logger.lifecycle("Creating directory structure")
        this.frameworkMainDir.mkdirs()
        this.frameworkAppDir = new File(frameworkMainDir, conf.projectName + ".framework")
        this.frameworkAppDir.mkdirs()
        this.frameworkVersionsDir = new File(frameworkAppDir, "Versions")
        this.frameworkVersionsDir.mkdirs()
        this.frameworkVersionsVersionDir = new File(frameworkVersionsDir, frameworkVersion)
        this.frameworkVersionsVersionDir.mkdirs()
        this.frameworkVersionsVersionResourcesDir = new File(frameworkVersionsVersionDir, "Resources")
        this.frameworkVersionsVersionResourcesDir.mkdirs()
        this.frameworkVersionsVersionHeadersDir = new File(frameworkVersionsVersionDir, "Headers")
        this.frameworkVersionsVersionHeadersDir.mkdirs()
    }

    private xcodeBuilds() {
        projectHelper.executeCommand(project, iosConf.getXCodeBuildExecutionPath() + [
                "-target",
                frameworkTarget,
                "-configuration",
                frameworkConfiguration,
                "-sdk",
                iosConf.simulatorSDK,
                "-arch",
                "i386",
                "clean",
                "build"
        ])
        projectHelper.executeCommand(project, iosConf.getXCodeBuildExecutionPath() + [
                "-target",
                frameworkTarget,
                "-configuration",
                frameworkConfiguration,
                "-sdk",
                iosConf.
                        sdk,
                "clean",
                "build"
        ])
    }
}
