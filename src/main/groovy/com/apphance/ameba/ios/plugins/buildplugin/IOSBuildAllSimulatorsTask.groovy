package com.apphance.ameba.ios.plugins.buildplugin

import java.io.File

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AmebaArtifact
import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSArtifactBuilderInfo;
import com.apphance.ameba.ios.IOSXCodeOutputParser;
import com.apphance.ameba.ios.IOSProjectConfiguration;
import com.apphance.ameba.ios.MPParser

/**
 * Builds iOS simulator projects
 */
class IOSBuildAllSimulatorsTask extends DefaultTask {

    Logger logger = Logging.getLogger(IOSBuildAllSimulatorsTask.class)
    ProjectHelper projectHelper
    ProjectConfiguration conf
    IOSProjectConfiguration iosConf
    IOSXCodeOutputParser iosConfigurationAndTargetRetriever = new IOSXCodeOutputParser()

    IOSBuildAllSimulatorsTask() {
        use (PropertyCategory) {
            this.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
            this.description = 'Builds all simulators for the project'
            this.projectHelper = new ProjectHelper();
            this.conf = project.getProjectConfiguration()
            this.dependsOn(project.readProjectConfiguration)
            this.dependsOn(project.copyMobileProvision)
        }
    }

    @TaskAction
    void buildAllSimulators() {
        iosConf = iosConfigurationAndTargetRetriever.getIosProjectConfiguration(project)
        iosConf.targets.each { target ->
            buildDebugRelease(project, target)
        }
    }

    void buildDebugRelease(Project project, String target) {
        def configuration = "Debug"
        logger.lifecycle( "\n\n\n=== Building DEBUG target ${target}, configuration ${configuration}  ===")
        if (conf.versionString != null) {
            if (System.getenv()["SKIP_IOS_BUILDS"] != null) {
                logger.lifecycle ("********************* CAUTION !!!! *********************************")
                logger.lifecycle ("* Skipping iOS builds because SKIP_IOS_BUILDS variable is set  *")
                logger.lifecycle ("* This should never happen on actual jenkins build                 *")
                logger.lifecycle ("* If it does make sure that SKIP_IOS_BUILDS variable is unset    *")
                logger.lifecycle ("********************************************************************")
            } else {
                projectHelper.executeCommand(project, [
                    "xcodebuild" ,
                    "-target",
                    target,
                    "-configuration",
                    configuration,
                    "-sdk",
                    iosConf.simulatorsdk
                ])
            }
            IOSArtifactBuilderInfo bi= new IOSArtifactBuilderInfo()
            bi.target = target
            bi.configuration = configuration
            bi.buildDirectory = new File(project.file( "build"),"${configuration}-iphonesimulator")
            bi.fullReleaseName  = "${target}-${configuration}-${conf.fullVersionString}"
            bi.folderPrefix = "${conf.projectDirectoryName}/${conf.fullVersionString}/${target}/${configuration}"
            bi.filePrefix = "${target}-${configuration}-${conf.fullVersionString}"
            bi.mobileprovisionFile = iosConfigurationAndTargetRetriever.findMobileProvisionFile(project, bi.target, bi.configuration)
            bi.plistFile = iosConf.plistFile
            iosConf.families.each { device ->
                bi.id = "${device}-${target}"
                prepareSimulatorBundleFile(project,bi,device)
            }
        } else {
            logger.lifecycle("Skipping building debug artifacts -> the build is not versioned")
        }
    }

    void prepareSimulatorBundleFile(Project project, IOSArtifactBuilderInfo bi, String device) {
        AmebaArtifact file = new AmebaArtifact()
        file.name = "Simulator build for ${device}"
        file.url = new URL(conf.baseUrl, "${bi.folderPrefix}/${bi.filePrefix}-${device}-simulator-image.dmg")
        file.location = new File(conf.otaDirectory,"${bi.folderPrefix}/${bi.filePrefix}-${device}-simulator-image.dmg")
        file.location.parentFile.mkdirs()
        file.location.delete()
        def File tmpDir = File.createTempFile("${conf.projectName}-${bi.target}-${device}-simulator",".tmp")
        tmpDir.delete()
        tmpDir.mkdir()
        destDir = new File(tmpDir,"${bi.target} (${device}_Simulator).app")
        destDir.mkdir()
        rsyncTemplatePreservingExecutableFlag(project,destDir)
        File embedDir = new File(destDir, "Contents/Resources/EmbeddedApp")
        embedDir.mkdirs()
        File sourceApp = new File(bi.buildDirectory,"${bi.target}.app")
        rsyncEmbeddedAppPreservingExecutableFlag(project,sourceApp, embedDir)
        updateBundleId(project, bi, destDir)
        resampleIcon(project, destDir)
        updateDeviceFamily(device, embedDir, bi, project)
        String [] prepareDmgCommand = [
            "hdiutil",
            "create",
            file.location,
            "-srcfolder",
            destDir,
            "-volname",
            "${conf.projectName}-${bi.target}-${device}"
        ]
        projectHelper.executeCommand(project, prepareDmgCommand)
        iosConf.dmgImageFiles.put(bi.id,file)
        logger.lifecycle("Simulator zip file created: ${file}")
    }

    private rsyncTemplatePreservingExecutableFlag(Project project, File destDir) {
        String [] rsyncCommand = [
            "rsync",
            "-aE",
            "--exclude",
            "Contents/Resources/EmbeddedApp",
            "/Applications/Simulator Bundler.app/Contents/Resources/Launcher.app/",
            destDir
        ]
        projectHelper.executeCommand(project, rsyncCommand)
    }

    private rsyncEmbeddedAppPreservingExecutableFlag(Project project, File sourceAppDir, File embedDir) {
        String [] rsyncCommand = [
            "rsync",
            "-aE",
            sourceAppDir,
            embedDir
        ]
        projectHelper.executeCommand(project, rsyncCommand)
    }


    private updateDeviceFamily(String device, embedDir, IOSArtifactBuilderInfo bi, Project project) {
        File targetPlistFile = new File(embedDir, "${bi.target}.app/Info.plist")
        String [] deleteDeviceFamilyCommand = [
            "/usr/libexec/PlistBuddy",
            "-c",
            "Delete UIDeviceFamily",
            targetPlistFile
        ]
        projectHelper.executeCommand(project, deleteDeviceFamilyCommand, false)
        String [] addDeviceFamilyCommand = [
            "/usr/libexec/PlistBuddy",
            "-c",
            "Add UIDeviceFamily array",
            targetPlistFile
        ]
        projectHelper.executeCommand(project, addDeviceFamilyCommand)
        String family = (device == "iPhone" ? "1" : "2")
        String [] updateDeviceFamilyCommand = [
            "/usr/libexec/PlistBuddy",
            "-c",
            "Add UIDeviceFamily:0 integer ${family}",
            targetPlistFile
        ]
        projectHelper.executeCommand(project, updateDeviceFamilyCommand)
    }

    private resampleIcon(Project project, File tmpDir) {
        String [] iconResampleCommand = [
            "/opt/local/bin/convert",
            conf.iconFile,
            "-resample",
            "128x128",
            new File(tmpDir,"Contents/Resources/Launcher.icns")
        ]
        projectHelper.executeCommand(project, iconResampleCommand)
    }

    private updateBundleId(Project project, IOSArtifactBuilderInfo bi, File tmpDir) {
        def bundleId = MPParser.readBundleIdFromProvisionFile(bi.mobileprovisionFile.toURI().toURL())
        String [] setBundleIdCommand = [
            "/usr/libexec/PlistBuddy",
            "-c",
            "Set :CFBundleIdentifier ${bundleId}.launchsim",
            new File(tmpDir,"Contents/Info.plist")
        ]
        projectHelper.executeCommand(project, setBundleIdCommand)
    }
}
