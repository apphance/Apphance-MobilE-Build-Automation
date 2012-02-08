package com.apphance.ameba.ios.plugins;

import groovy.io.FileType

import java.io.File
import java.util.Collection

import javax.xml.parsers.DocumentBuilderFactory

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.XMLBomAwareFileReader
import com.apphance.ameba.ios.IOSConfigurationAndTargetRetriever
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.IOSBuildAllSimulatorsTask
import com.apphance.ameba.ios.IOSSingleReleaseBuilder
import com.apphance.ameba.ios.IOSVerifySetupTask
import com.sun.org.apache.xpath.internal.XPathAPI

/**
 * Plugin for various X-Code related tasks.
 * Requires plistFileName set in project properties
 * (set to point to main project .plist file)
 *
 */
class IOSPlugin implements Plugin<Project> {
    static Logger logger = Logging.getLogger(IOSPlugin.class)

    String pListFileName
    ProjectHelper projectHelper
    ProjectConfiguration conf
    IOSProjectConfiguration iosConf

    IOSConfigurationAndTargetRetriever iosConfigurationAndTargetRetriever

    def void apply (Project project) {
        this.projectHelper = new ProjectHelper();
        this.conf = projectHelper.getProjectConfiguration(project)
        this.iosConfigurationAndTargetRetriever = new IOSConfigurationAndTargetRetriever()
        this.iosConf = iosConfigurationAndTargetRetriever.getIosProjectConfiguration(project)
        readIosProjectConfiguration(project)
        readIosTargetsAndConfigurations(project)
        prepareReadIosProjectVersionsTask(project)
        prepareUpdateVersionTask(project)
        prepareCleanTask(project)
        prepareUnlockKeyChainTask(project)
        prepareCopyMobileProvisionTask(project)
        prepareBuildSingleReleaseTask(project)
        project.task('buildAllSimulators', type: IOSBuildAllSimulatorsTask)
        project.task('verifyIOSSetup',type: IOSVerifySetupTask)
        prepareBuildAllTask(project)
        preparePreReleaseTask(project)
        prepareReplaceBundleIdPrefixTask(project)
        addIosSourceExcludes()
    }

    private addIosSourceExcludes() {
        conf.sourceExcludes << '**/build/**'
    }

    private readIosProjectConfiguration(Project project) {
        this.pListFileName = project['ios.plist.file']
        iosConf.mainTarget = project.hasProperty('ios.mainTarget')  ? project['ios.mainTarget'] :  null
        iosConf.mainConfiguration = project.hasProperty('ios.mainConfiguration')  ? project['ios.mainConfiguration'] : null
        iosConf.sdk = project.hasProperty('ios.sdk') ? project['ios.sdk'] : 'iphoneos'
        iosConf.simulatorsdk = project.hasProperty('ios.simulator.sdk') ? project['ios.simulator.sdk'] : 'iphonesimulator'
        iosConf.plistFile = new File(this.pListFileName)
        iosConf.distributionDirectory =  new File(project.rootDir, project['ios.distribution.resources.dir'])
        iosConf.families = project.hasProperty('ios.families') ? project['ios.families'].split(",")*.trim() : ["iPhone", "iPad"]
        iosConf.excludedBuilds = project.hasProperty('ios.excluded.builds') ? project['ios.excluded.builds'].split(",")*.trim() : []
        iosConf.foneMonkeyConfiguration = project.hasProperty('ios.fonemonkey.configuration') ? project['ios.fonemonkey.configuration'] : "Debug"
        iosConf.KIFConfiguration = project.hasProperty('ios.kif.configuration') ? project['ios.kif.configuration'] : "Debug"
        if (iosConf.plistFile != null) {
            conf.commitFilesOnVCS << iosConf.plistFile.absolutePath
        }
    }

    def void readIosTargetsAndConfigurations(Project project) {
        project.file("bin").mkdirs()
        def lines = projectHelper.executeCommand(project, ["xcodebuild", "-list"]as String[],false, null, null, 1, true)
        def trimmed = lines*.trim()
        IOSProjectConfiguration iosConf = iosConfigurationAndTargetRetriever.getIosProjectConfiguration(project)
        conf.projectName =  iosConfigurationAndTargetRetriever.readProjectName(trimmed)
        iosConf.targets = iosConfigurationAndTargetRetriever.readBuildableTargets(trimmed)
        iosConf.configurations = iosConfigurationAndTargetRetriever.readBuildableConfigurations(trimmed)
        iosConf.alltargets = iosConfigurationAndTargetRetriever.readBaseTargets(trimmed, { true })
        iosConf.allconfigurations = iosConfigurationAndTargetRetriever.readBaseConfigurations(trimmed, { true })
        if (iosConf.targets == ['']) {
            logger.lifecycle("Please specify at least one target")
            iosConf.targets = []
        }
        if (iosConf.configurations == ['']) {
            logger.lifecycle("Please specify at least one configuration")
            iosConf.configurations = []
        }
        if (iosConf.mainTarget == null) {
            iosConf.mainTarget = iosConf.targets.empty? null : iosConf.targets[0]
        }
        if (iosConf.mainConfiguration == null) {
            iosConf.mainConfiguration = iosConf.configurations.empty ? null : iosConf.configurations[0]
        }
        logger.lifecycle("Standard buildable targets: " + iosConf.targets)
        logger.lifecycle("Standard buildable configurations : " + iosConf.configurations)
        logger.lifecycle("Main target: " + iosConf.mainTarget)
        logger.lifecycle("Main configuration : " + iosConf.mainConfiguration)
        logger.lifecycle("All targets: " + iosConf.alltargets)
        logger.lifecycle("All configurations : " + iosConf.allconfigurations)
    }


    void prepareReadIosProjectVersionsTask(Project project) {
        def task = project.task('readIOSProjectVersions')
        task.group = AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
        task.description = 'Reads iOS project version information'
        task << {
            this.pListFileName = project['ios.plist.file']
            def root = getParsedPlist(project)
            XPathAPI.selectNodeList(root,
                    '/plist/dict/key[text()="CFBundleShortVersionString"]').each{
                        conf.versionString =  it.nextSibling.nextSibling.textContent
                    }
            XPathAPI.selectNodeList(root,
                    '/plist/dict/key[text()="CFBundleVersion"]').each{
                        def versionCodeString = it.nextSibling.nextSibling.textContent
                        try {
                            conf.versionCode = versionCodeString.toInteger()
                        } catch (NumberFormatException e) {
                            logger.lifecycle("Format of the ${versionCodeString} is not numeric. Starting from 1.")
                            conf.versionCode = 0
                        }
                    }
            if (!projectHelper.isPropertyOrEnvironmentVariableDefined(project, 'version.string')) {
                conf.versionString = conf.versionString + "-SNAPSHOT"
            }
        }
        project.readProjectConfiguration.dependsOn(task)
    }

    void prepareBuildAllTask(Project project) {
        def task = project.task('buildAll')
        task.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
        task.description = 'Builds all target/configuration combinations and produces all artifacts (zip, ipa, messages, etc)'
        def targets = iosConf.targets
        def configurations = iosConf.configurations
        logger.lifecycle("Building all builds")
        targets.each { target ->
            configurations.each { configuration ->
                def id = "${target}-${configuration}".toString()
                if (!iosConf.isBuildExcluded(id)) {
                    def noSpaceId = id.replaceAll(' ','_')
                    def singleTask = project.task("build-${noSpaceId}")
                    singleTask.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
                    singleTask.description = "Builds target:${target} configuration:${configuration}"
                    singleTask << {
                        def singleReleaseBuilder = new IOSSingleReleaseBuilder(project, project.ant)
                        singleReleaseBuilder.buildRelease(project, target, configuration)
                    }
                    task.dependsOn(singleTask)
                    singleTask.dependsOn(project.readProjectConfiguration)
                    singleTask.dependsOn(project.copyMobileProvision)
                } else {
                    logger.lifecycle("Skipping build ${id} - it is excluded in configuration (${iosConf.excludedBuilds})")
                }
            }
        }
    }


    private org.w3c.dom.Element getParsedPlist(Project project) {
        File pListFile = new File("${project.rootDir}/${pListFileName}")
        logger.debug("Reading file " + pListFile)
        return new XMLBomAwareFileReader().readXMLFileIncludingBom(pListFile)
    }


    private org.w3c.dom.Element getParsedPlist(File file) {
        def builder     = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        return new XMLBomAwareFileReader().readXMLFileIncludingBom(file)
    }


    def void prepareUpdateVersionTask(Project project) {
        def task = project.task('updateVersion')
        task.group = AmebaCommonBuildTaskGroups.AMEBA_RELEASE
        task.description = """Updates version stored in plist file of the project.
           Numeric version is (incremented), String version is set from version.string property"""
        task << {
            conf.versionString = projectHelper.readPropertyOrEnvironmentVariable(project,'version.string')
            def root = getParsedPlist(project)
            XPathAPI.selectNodeList(root,
                    '/plist/dict/key[text()="CFBundleShortVersionString"]').each{
                        it.nextSibling.nextSibling.textContent = conf.versionString
                    }
            conf.versionCode += 1
            XPathAPI.selectNodeList(root,
                    '/plist/dict/key[text()="CFBundleVersion"]').each{
                        it.nextSibling.nextSibling.textContent = conf.versionCode
                    }
            new File("${project.rootDir}/${pListFileName}").write(root as String)
            logger.lifecycle("New version code: ${conf.versionCode}")
            logger.lifecycle("Updated version string to ${conf.versionString}")
        }
        task.dependsOn(project.readProjectConfiguration)
    }

    def void prepareBuildSingleReleaseTask(Project project) {
        def task = project.task('buildSingleRelease')
        task.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
        task.description = "Builds single release for iOS. Requires ios.target and ios.configuration properties"
        task << {
            def singleReleaseBuilder = new IOSSingleReleaseBuilder(project, this.ant)
            String target = projectHelper.getExpectedProperty(project, "ios.target")
            String configuration = projectHelper.getExpectedProperty(project, "ios.configuration")
            singleReleaseBuilder.buildRelease(project, target, configuration)
        }
        task.dependsOn(project.readProjectConfiguration)
    }


    def void prepareCleanTask(Project project) {
        def task = project.task('clean')
        task.description = "Cleans the project"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
        task << {
            projectHelper.executeCommand(project, ["dot_clean", "./"]as String [])
            ant.delete(dir: project.file("build"), verbose: true)
            ant.delete(dir: project.file("bin"), verbose: true)
            ant.delete(dir: project.file("documentation"), verbose: true)
        }
        project.cleanRelease.dependsOn(task)
        task.dependsOn(project.cleanConfiguration)
    }

    def void prepareUnlockKeyChainTask(Project project) {
        def task = project.task('unlockKeyChain')
        task.description = """Unlocks key chain used during project building.
              Requires osx.keychain.password and osx.keychain.location properties
              or OSX_KEYCHAIN_PASSWORD and OSX_KEYCHAIN_LOCATION"""
        task.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
        task << {
            def keychainPassword = projectHelper.readPropertyOrEnvironmentVariable(project, "osx.keychain.password")
            def keychainLocation = projectHelper.readPropertyOrEnvironmentVariable(project, "osx.keychain.location")
            projectHelper.executeCommand(project, [
                "security",
                "unlock-keychain",
                "-p",
                keychainPassword,
                keychainLocation
            ])
        }
        task.dependsOn(project.readProjectConfiguration)
    }

    def void prepareCopyMobileProvisionTask(Project project) {
        def task = project.task('copyMobileProvision')
        task.description = "Copies mobile provision file to the user library"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
        task << {
            userHome = System.getProperty("user.home")
            def mobileProvisionDirectory = userHome + "/Library/MobileDevice/Provisioning Profiles/"
            new File(mobileProvisionDirectory).mkdirs()
            ant.copy(todir: mobileProvisionDirectory, overwrite: true) {
                fileset(dir: iosConf.distributionDirectory) { include(name: "*.mobileprovision") }
            }
        }
        task.dependsOn(project.readProjectConfiguration)
    }


    private void preparePreReleaseTask(Project project) {
        def task = project.task('preRelease')
        task.description = "Performs standard pre-release operations"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_RELEASE
        task << { logger.lifecycle("Performed pre-release operations") }
        task.dependsOn(project.verifyReleaseNotes)
        if (project.hasProperty('cleanVCS')) {
            task.dependsOn(project.cleanVCS)
        }
        task.dependsOn(project.updateVersion)
    }

    private void prepareReplaceBundleIdPrefixTask(Project project) {
        def task = project.task('replaceBundleIdPrefix')
        task.description = """Replaces bundleId prefix with a new one. Requires oldBundleIdPrefix and newBundleIdPrefix
           parameters. The .mobileprovision files need to be in 'newBundleIdPrefix' sub-directory of distribution directory"""
        task.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
        task << {
            def oldBundleIdPrefix = projectHelper.getExpectedProperty(project, "oldBundleIdPrefix")
            logger.lifecycle("Old bundleId ${oldBundleIdPrefix}")
            def newBundleIdPrefix = projectHelper.getExpectedProperty(project, "newBundleIdPrefix")
            logger.lifecycle("New bundleId ${newBundleIdPrefix}")
            replaceBundleInAllPlists(project, newBundleIdPrefix, oldBundleIdPrefix)
            replaceBundleInAllSourceFiles(project, newBundleIdPrefix, oldBundleIdPrefix)
            iosConf.distributionDirectory = new File(iosConf.distributionDirectory, newBundleIdPrefix)
            logger.lifecycle("New distribution directory: ${iosConf.distributionDirectory}")
            logger.lifecycle("Replaced the bundleIdprefix everywhere")
        }
    }

    private void replaceBundleInAllPlists(Project project, String newBundleIdPrefix, String oldBundleIdPrefix) {
        logger.lifecycle("Finding all plists")
        def plistFiles = findAllPlistFiles(project)
        plistFiles.each {  file ->
            def root = getParsedPlist(file)
            XPathAPI.selectNodeList(root,'/plist/dict/key[text()="CFBundleIdentifier"]').each {
                String bundleToReplace = it.nextSibling.nextSibling.textContent
                if (bundleToReplace.startsWith(oldBundleIdPrefix)) {
                    String newResult = newBundleIdPrefix + bundleToReplace.substring(oldBundleIdPrefix.length())
                    it.nextSibling.nextSibling.textContent  = newResult
                    file.write(root as String)
                    logger.lifecycle("Replaced the bundleId to ${newResult} from ${bundleToReplace} in ${file}")
                } else if (bundleToReplace.startsWith(newBundleIdPrefix)) {
                    logger.lifecycle("Already replaced the bundleId to ${bundleToReplace} in ${file}")
                } else {
                    throw new GradleException("The bundle to replace ${bundleToReplace} does not start with expected ${oldBundleIdPrefix} in ${file}. Not replacing !!!!!!!.")
                }
            }
        }
        logger.lifecycle("Finished processing all plists")
    }

    private void replaceBundleInAllSourceFiles(Project project, String newBundleIdPrefix, String oldBundleIdPrefix) {
        logger.lifecycle("Finding all source files")
        def sourceFiles = findAllSourceFiles(project)
        String valueToFind = 'bundleWithIdentifier:@"' + oldBundleIdPrefix
        String valueToReplace = 'bundleWithIdentifier:@"' + newBundleIdPrefix
        sourceFiles.each {  file ->
            String t = file.text
            if (t.contains(valueToFind)) {
                file.write(t.replace(valueToFind, valueToReplace))
                logger.lifecycle("Replaced the ${valueToFind} with ${valueToReplace} in ${file}")
            }
        }
        logger.lifecycle("Finished processing all source files")
    }

    Collection<File> findAllPlistFiles(Project project) {
        def result = []
        project.rootDir.eachFileRecurse(FileType.FILES, {
            if (it.name.endsWith("-Info.plist") && !it.path.contains("/External/") && !it.path.contains('/build/')) {
                logger.lifecycle("Adding plist file ${it} to processing list")
                result << it
            }
        })
        return result
    }

    Collection<File> findAllSourceFiles(Project project) {
        def result = []
        project.rootDir.eachFileRecurse(FileType.FILES, {
            if ((it.name.endsWith(".m") || it.name.endsWith(".h")) && !it.path.contains("/External/")) {
                logger.lifecycle("Adding source file ${it} to processing list")
                result << it
            }
        })
        return result
    }
}
