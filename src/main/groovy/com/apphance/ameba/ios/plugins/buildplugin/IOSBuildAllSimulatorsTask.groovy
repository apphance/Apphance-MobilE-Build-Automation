package com.apphance.ameba.ios.plugins.buildplugin

import java.io.File

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSBuilderInfo;
import com.apphance.ameba.ios.IOSXCodeOutputParser;
import com.apphance.ameba.ios.IOSProjectConfiguration;
import com.apphance.ameba.ios.MPParser
import com.apphance.ameba.ios.plugins.release.IOSReleaseConfiguration;
import com.apphance.ameba.ios.plugins.release.IOSReleaseConfigurationRetriever;
import com.apphance.ameba.plugins.release.AmebaArtifact;
import com.apphance.ameba.plugins.release.ProjectReleaseCategory;
import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration;

/**
 * Builds iOS simulator projects
 */
class IOSBuildAllSimulatorsTask extends DefaultTask {

    Logger logger = Logging.getLogger(IOSBuildAllSimulatorsTask.class)
    ProjectHelper projectHelper
    ProjectConfiguration conf
    IOSProjectConfiguration iosConf
    ProjectReleaseConfiguration releaseConf
    IOSReleaseConfiguration iosReleaseConf
    IOSSingleVariantBuilder iosSingleVariantBuilder

    IOSBuildAllSimulatorsTask() {
        this.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
        this.description = 'Builds all simulators for the project'
        this.projectHelper = new ProjectHelper();
        this.conf = PropertyCategory.getProjectConfiguration(project)
        this.releaseConf = ProjectReleaseCategory.getProjectReleaseConfiguration(project)
        this.iosSingleVariantBuilder = new IOSSingleVariantBuilder(project, project.ant)
        this.dependsOn(project.readProjectConfiguration)
        this.dependsOn(project.copyMobileProvision)
    }

    String getFolderPrefix(IOSBuilderInfo bi) {
        return "${releaseConf.projectDirectoryName}/${conf.fullVersionString}/${bi.target}/${bi.configuration}"
    }


    @TaskAction
    void buildAllSimulators() {
        iosConf = IOSXCodeOutputParser.getIosProjectConfiguration(project)
        iosReleaseConf = IOSReleaseConfigurationRetriever.getIosReleaseConfiguration(project)
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
                projectHelper.executeCommand(project, iosConf.getXCodeBuildExecutionPath() + [
                    "-target",
                    target,
                    "-configuration",
                    configuration,
                    "-sdk",
                    iosConf.simulatorsdk
                ])
            }
            IOSBuilderInfo bi= new IOSBuilderInfo()
            bi.target = target
            bi.configuration = configuration
            bi.buildDirectory = new File(project.file(iosSingleVariantBuilder.tmpDir(target, configuration) + "/build"),"${configuration}-iphonesimulator")
            bi.fullReleaseName  = "${target}-${configuration}-${conf.fullVersionString}"
            bi.filePrefix = "${target}-${configuration}-${conf.fullVersionString}"
            bi.mobileprovisionFile = IOSXCodeOutputParser.findMobileProvisionFile(project, bi.target, bi.configuration)
            bi.plistFile = iosConf.plistFile
            iosConf.families.each { device ->
                bi.id = "${device}-${target}"
                prepareSimulatorBundleFile(project,bi,device)
            }
        } else {
            logger.lifecycle("Skipping building debug artifacts -> the build is not versioned")
        }
    }

    void prepareSimulatorBundleFile(Project project, IOSBuilderInfo bi, String device) {
        AmebaArtifact file = new AmebaArtifact()
        file.name = "Simulator build for ${device}"
        file.url = new URL(releaseConf.baseUrl, "${getFolderPrefix(bi)}/${bi.filePrefix}-${device}-simulator-image.dmg")
        file.location = new File(releaseConf.otaDirectory,"${getFolderPrefix(bi)}/${bi.filePrefix}-${device}-simulator-image.dmg")
        file.location.parentFile.mkdirs()
        file.location.delete()
        def File tmpDir = File.createTempFile("${conf.projectName}-${bi.target}-${device}-simulator",".tmp")
        tmpDir.delete()
        tmpDir.mkdir()
        def destDir = new File(tmpDir,"${bi.target} (${device}_Simulator).app")
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
        iosReleaseConf.dmgImageFiles.put(bi.id,file)
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


    private updateDeviceFamily(String device, embedDir, IOSBuilderInfo bi, Project project) {
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
            releaseConf.iconFile,
            "-resample",
            "128x128",
            new File(tmpDir,"Contents/Resources/Launcher.icns")
        ]
        projectHelper.executeCommand(project, iconResampleCommand)
    }

    private updateBundleId(Project project, IOSBuilderInfo bi, File tmpDir) {
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
