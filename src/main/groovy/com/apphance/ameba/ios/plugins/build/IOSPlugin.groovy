package com.apphance.ameba.ios.plugins.build;



import javax.xml.parsers.DocumentBuilderFactory

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory;
import com.apphance.ameba.XMLBomAwareFileReader
import com.apphance.ameba.ios.IOSConfigurationAndTargetRetriever
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.IOSProjectProperty
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
        use (PropertyCategory) {
            this.projectHelper = new ProjectHelper();
            this.conf = project.getProjectConfiguration()
            this.iosConfigurationAndTargetRetriever = new IOSConfigurationAndTargetRetriever()
            this.iosConf = iosConfigurationAndTargetRetriever.getIosProjectConfiguration(project)
            prepareReadIosProjectConfigurationTask(project)
            prepareReadIosTargetsAndConfigurationsTask(project)
            prepareReadIosProjectVersionsTask(project)
            prepareUpdateVersionTask(project)
            prepareCleanTask(project)
            prepareUnlockKeyChainTask(project)
            prepareCopyMobileProvisionTask(project)
            prepareBuildSingleReleaseTask(project)
            project.task('buildAllSimulators', type: IOSBuildAllSimulatorsTask)
            project.task('verifyIOSSetup', type: IOSVerifySetupTask)
            prepareBuildAllTask(project)
            preparePreReleaseTask(project)
            prepareReplaceBundleIdPrefixTask(project)
            addIosSourceExcludes()
            prepareShowIOSPropertiesTask(project)
            project.task('prepareIOSSetup', type:IOSPrepareSetupTask)
        }
    }

    private prepareShowIOSPropertiesTask(Project project) {
        def task =  project.task('showIOSProperties')
        task.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        task.description = 'Prints all ios project properties'
        task.dependsOn(project.readProjectConfiguration)
        project.showProperties.dependsOn(task)
        task << {
            use (PropertyCategory) {
                System.out.println(project.listPropertiesAsString(IOSProjectProperty.class, true))
            }
        }
    }

    private addIosSourceExcludes() {
        conf.sourceExcludes << '**/build/**'
    }

    private prepareReadIosProjectConfigurationTask(Project project) {
        def task = project.task('readIOSProjectConfiguration')
        task.group = AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
        task.description = 'Reads iOS project configuration'
        task << {
            use (PropertyCategory) {
                this.pListFileName = project.readProperty(IOSProjectProperty.PLIST_FILE)
                iosConf.mainTarget = project.readProperty(IOSProjectProperty.MAIN_TARGET)
                iosConf.mainConfiguration = project.readProperty(IOSProjectProperty.MAIN_CONFIGURATION)
                iosConf.sdk = project.readProperty(IOSProjectProperty.IOS_SDK,'iphoneos')
                iosConf.simulatorsdk = project.readProperty(IOSProjectProperty.IOS_SIMULATOR_SDK, 'iphonesimulator')
                iosConf.plistFile = new File(this.pListFileName)
                iosConf.distributionDirectory =  new File(project.rootDir, project.readProperty(IOSProjectProperty.DISTRIBUTION_DIR))
                iosConf.families = project.readProperty(IOSProjectProperty.IOS_FAMILIES, 'iPhone,iPad').split(",")*.trim()
                iosConf.excludedBuilds = project.readProperty(IOSProjectProperty.EXCLUDED_BUILDS,'').split(",")*.trim()
                iosConf.KIFConfiguration = project.hasProperty('ios.kif.configuration') ? project['ios.kif.configuration'] : "Debug"
                if (iosConf.plistFile != null) {
                    conf.commitFilesOnVCS << iosConf.plistFile.absolutePath
                }
            }
        }
        project.readProjectConfiguration.dependsOn(task)
    }

    def void prepareReadIosTargetsAndConfigurationsTask(Project project) {
        def task = project.task('readIOSProjectTargetAndConfiguration')
        task.group = AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
        task.description = 'Reads iOS targets and configurations'
        task << {
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
        project.readProjectConfiguration.dependsOn(task)
    }


    void prepareReadIosProjectVersionsTask(Project project) {
        def task = project.task('readIOSProjectVersions')
        task.group = AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
        task.description = 'Reads iOS project version information'
        task << {
            use (PropertyCategory) {
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
                if (!project.isPropertyOrEnvironmentVariableDefined('version.string')) {
                    logger.lifecycle("Version string is updated to SNAPSHOT because it is not release build")
                    conf.versionString = conf.versionString + "-SNAPSHOT"
                } else {
                    logger.lifecycle("Version string is not updated to SNAPSHOT because it is release build")
                }
            }
        }
        project.readProjectConfiguration.dependsOn(task)
    }

    void prepareBuildAllTask(Project project) {
        def task = project.task('buildAll')
        task.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
        task.description = 'Builds all target/configuration combinations and produces all artifacts (zip, ipa, messages, etc)'
        def lines = projectHelper.executeCommand(project, ["xcodebuild", "-list"]as String[],false, null, null, 1, true)
        def trimmed = lines*.trim()
        iosConf.targets = iosConfigurationAndTargetRetriever.readBuildableTargets(trimmed)
        iosConf.configurations = iosConfigurationAndTargetRetriever.readBuildableConfigurations(trimmed)
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
        task.dependsOn(project.readProjectConfiguration)
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
            use (PropertyCategory) {
                conf.versionString = project.readPropertyOrEnvironmentVariable('version.string')
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
        }
        task.dependsOn(project.readProjectConfiguration)
    }

    def void prepareBuildSingleReleaseTask(Project project) {
        def task = project.task('buildSingleRelease')
        task.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
        task.description = "Builds single release for iOS. Requires ios.target and ios.configuration properties"
        task << {
            use (PropertyCategory) {
                def singleReleaseBuilder = new IOSSingleReleaseBuilder(project, this.ant)
                String target = project.readExpectedProperty("ios.target")
                String configuration = project.readExpectedProperty("ios.configuration")
                singleReleaseBuilder.buildRelease(project, target, configuration)
            }
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
            use(PropertyCategory) {
                def keychainPassword = project.readPropertyOrEnvironmentVariable("osx.keychain.password")
                def keychainLocation = project.readPropertyOrEnvironmentVariable("osx.keychain.location")
                projectHelper.executeCommand(project, [
                    "security",
                    "unlock-keychain",
                    "-p",
                    keychainPassword,
                    keychainLocation
                ])
            }
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
            use (PropertyCategory) {
                def oldBundleIdPrefix = project.readExpectedProperty("oldBundleIdPrefix")
                logger.lifecycle("Old bundleId ${oldBundleIdPrefix}")
                def newBundleIdPrefix = project.readExpectedProperty("newBundleIdPrefix")
                logger.lifecycle("New bundleId ${newBundleIdPrefix}")
                replaceBundleInAllPlists(project, newBundleIdPrefix, oldBundleIdPrefix)
                replaceBundleInAllSourceFiles(project, newBundleIdPrefix, oldBundleIdPrefix)
                iosConf.distributionDirectory = new File(iosConf.distributionDirectory, newBundleIdPrefix)
                logger.lifecycle("New distribution directory: ${iosConf.distributionDirectory}")
                logger.lifecycle("Replaced the bundleIdprefix everywhere")
            }
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
