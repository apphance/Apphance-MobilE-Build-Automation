package com.apphance.ameba.ios.plugins.framework


import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory;
import com.apphance.ameba.ios.IOSConfigurationAndTargetRetriever;
import com.apphance.ameba.ios.IOSProjectConfiguration;


/**
 * Builds iOS simulator projects
 */
class IOSBuildFrameworkTask extends DefaultTask {

    static final String FRAMEWORK_BUILD_PATH = 'Development-Framework'

    Logger logger = Logging.getLogger(IOSBuildFrameworkTask.class)
    ProjectHelper projectHelper
    ProjectConfiguration conf
    IOSProjectConfiguration iosConf
    IOSConfigurationAndTargetRetriever iosConfigurationAndTargetRetriever = new IOSConfigurationAndTargetRetriever()
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
        use (PropertyCategory) {
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
        use (PropertyCategory) {
            iosConf = project['ios.project.configuration']
            iosConf.frameworkTarget = project['ios.framework.target']
            iosConf.frameworkConfiguration = project['ios.framework.configuration']
            iosConf.frameworkVersion = project.readExpectedProperty('ios.framework.version')
            iosConf.frameworkCurrentVersion =  project.readExpectedProperty('ios.framework.currentVersion')
            iosConf.frameworkCompatibilityVersion = project.readExpectedProperty('ios.framework.compatibilityVersion')
            iosConf.frameworkHeaders = project.readExpectedProperty('ios.framework.headers').split(',')
            iosConf.frameworkResources = project.readExpectedProperty('ios.framework.resources').split(',')
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
        projectHelper.executeCommand(project,
                [
                    "lipo",
                    "-create",
                    this.iphoneosLibrary,
                    this.iphonesimulatorLibrary,
                    "-output",
                    new File(this.frameworkVersionsVersionDir, conf.projectName)]
                )
    }

    private copyingResources() {
        logger.lifecycle("Copying resources")
        iosConf.frameworkResources.each {
            if (it != '') {
                project.ant.copy(file: it, toDir: this.frameworkVersionsVersionResourcesDir)
            }
        }
    }

    private copyingHeaders() {
        logger.lifecycle("Copying headers")
        iosConf.frameworkHeaders.each {
            if (it != '') {
                project.ant.copy(file: it, toDir: this.frameworkVersionsVersionHeadersDir)
            }
        }
    }

    private setLinkLibraries() {
        logger.lifecycle("Set link libraries")
        this.iphoneosLibrary = new File(project.buildDir, iosConf.frameworkConfiguration + '-' +
                'iphoneos/lib' +  conf.projectName + '.a')
        this.iphonesimulatorLibrary = new File(project.buildDir, iosConf.frameworkConfiguration + '-' +
                'iphonesimulator/lib' +  conf.projectName + '.a')
    }

    private createSymlinks() {
        logger.lifecycle("Creating symlinks")
        projectHelper.executeCommand(project, frameworkVersionsDir, [
            "ln",
            "-s",
            iosConf.frameworkVersion,
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
        this.frameworkVersionsVersionDir = new File(frameworkVersionsDir, iosConf.frameworkVersion)
        this.frameworkVersionsVersionDir.mkdirs()
        this.frameworkVersionsVersionResourcesDir = new File(frameworkVersionsVersionDir, "Resources")
        this.frameworkVersionsVersionResourcesDir.mkdirs()
        this.frameworkVersionsVersionHeadersDir = new File(frameworkVersionsVersionDir, "Headers")
        this.frameworkVersionsVersionHeadersDir.mkdirs()
    }

    private xcodeBuilds() {
        projectHelper.executeCommand(project, [
            "xcodebuild" ,
            "-target",
            iosConf.frameworkTarget,
            "-configuration",
            iosConf.frameworkConfiguration,
            "-sdk",
            iosConf.simulatorsdk,
            "-arch",
            "i386",
            "clean",
            "build"
        ])
        projectHelper.executeCommand(project, [
            "xcodebuild" ,
            "-target",
            iosConf.frameworkTarget,
            "-configuration",
            iosConf.frameworkConfiguration,
            "-sdk",
            iosConf.
            sdk,
            "clean",
            "build"
        ])
    }
}
