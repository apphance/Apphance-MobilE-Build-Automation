package com.apphance.ameba.ios.plugins.buildplugin

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSBuilderInfo
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.IOSXCodeOutputParser
import com.apphance.ameba.ios.MPParser
import com.apphance.ameba.util.file.FileManager
import com.sun.org.apache.xpath.internal.XPathAPI
import groovy.io.FileType
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.xml.sax.SAXParseException

/**
 * Builds single variant for iOS projects.
 *
 */
class IOSSingleVariantBuilder {

    static Logger logger = Logging.getLogger(IOSSingleVariantBuilder.class)
    ProjectHelper projectHelper
    static Collection<IOSBuildListener> buildListeners = []
    ProjectConfiguration conf
    IOSProjectConfiguration iosConf
    AntBuilder ant
    Project project

    IOSSingleVariantBuilder(Project project, AntBuilder ant) {
        use(PropertyCategory) {
            this.project = project
            this.projectHelper = new ProjectHelper()
            this.conf = project.getProjectConfiguration()
            this.iosConf = IOSXCodeOutputParser.getIosProjectConfiguration(project)
            this.ant = ant
        }
    }

    private checkVersions() {
        logger.info("Application version: ${conf.versionCode} string: ${conf.versionString}")
        if (conf.versionCode == 0) {
            throw new GradleException("The CFBundleVersion key is missing from ${iosConf.plistFile} or its value is 0. Please add it or increase the value. Integers are only valid values")
        }
        if (conf.versionString.startsWith('NOVERSION')) {
            throw new GradleException("The CFBundleShortVersionString key is missing from ${iosConf.plistFile}. Please add it.")
        }
    }

    Collection<File> findAllPlistFiles(File dir) {
        def result = []
        dir.traverse([type: FileType.FILES, maxDepth: FileManager.MAX_RECURSION_LEVEL]) {
            if (it.name.endsWith(".plist") && !it.path.contains("/External/") && !it.path.contains('/build/')) {
                logger.lifecycle("Adding plist file ${it} to processing list")
                result << it
            }
        }
        return result
    }

    Collection<File> findAllSourceFiles(File dir) {
        def result = []
        dir.traverse([type: FileType.FILES, maxDepth: FileManager.MAX_RECURSION_LEVEL]) {
            if ((it.name.endsWith(".m") || it.name.endsWith(".h")) && !it.path.contains("/External/")) {
                logger.lifecycle("Adding source file ${it} to processing list")
                result << it
            }
        }
        return result
    }


    private void replaceBundleInAllPlists(File dir, String newBundleIdPrefix, String oldBundleIdPrefix) {
        logger.lifecycle("Finding all plists and replacing ${oldBundleIdPrefix} with ${newBundleIdPrefix}")
        def plistFiles = findAllPlistFiles(dir)
        plistFiles.each { file ->
            logger.lifecycle("Parsing ${file}")
            try {
                def root = MPParser.getParsedPlist(file)
                XPathAPI.selectNodeList(root, '/plist/dict/key[text()="CFBundleIdentifier"]').each {
                    String bundleToReplace = it.nextSibling.nextSibling.textContent
                    if (bundleToReplace.startsWith(oldBundleIdPrefix)) {
                        String newResult = newBundleIdPrefix + bundleToReplace.substring(oldBundleIdPrefix.length())
                        it.nextSibling.nextSibling.textContent = newResult
                        file.write(root as String)
                        logger.lifecycle("Replaced the bundleId to ${newResult} from ${bundleToReplace} in ${file}")
                    } else if (bundleToReplace.startsWith(newBundleIdPrefix)) {
                        logger.lifecycle("Already replaced the bundleId to ${bundleToReplace} in ${file}")
                    } else {
                        logger.warn("The bundle to replace ${bundleToReplace} does not start with expected ${oldBundleIdPrefix} in ${file}. Not replacing !!!!!!!.")
                    }
                }
            } catch (SAXParseException e) {
                logger.warn("Error when parsing ${file}: ${e}. Skipping.")
            }
        }
        logger.lifecycle("Finished processing all plists")
    }

    private void replaceBundleInAllSourceFiles(File dir, String newBundleIdPrefix, String oldBundleIdPrefix) {
        logger.lifecycle("Finding all source files")
        def sourceFiles = findAllSourceFiles(dir)
        String valueToFind = 'bundleWithIdentifier:@"' + oldBundleIdPrefix
        String valueToReplace = 'bundleWithIdentifier:@"' + newBundleIdPrefix
        sourceFiles.each { file ->
            String t = file.text
            if (t.contains(valueToFind)) {
                file.write(t.replace(valueToFind, valueToReplace))
                logger.lifecycle("Replaced the ${valueToFind} with ${valueToReplace} in ${file}")
            }
        }
        logger.lifecycle("Finished processing all source files")
    }

    void replaceBundleId(File dir, String oldBundleId, String newBundleId, String configuration) {
        replaceBundleInAllPlists(dir, newBundleId, oldBundleId)
        replaceBundleInAllSourceFiles(dir, newBundleId, oldBundleId)
        iosConf.distributionDirectories[configuration] = new File(iosConf.distributionDirectory, newBundleId)
        logger.lifecycle("New distribution directory: ${iosConf.distributionDirectories[configuration]}")
        logger.lifecycle("Replaced the bundleIdprefix everywhere")
    }

    void buildNormalVariant(Project project, String target, String configuration) {
        checkVersions()
        if (project.hasProperty('ios.bundleId.' + configuration)) {
            String newBundleId = project['ios.bundleId.' + configuration]
            String oldBundleId = MPParser.readBundleIdFromProvisionFile(
                    IOSXCodeOutputParser.findMobileProvisionFile(project, target, configuration, false).toURI().toURL());
            replaceBundleId(tmpDir(target, configuration), oldBundleId, newBundleId, configuration)
        }
        logger.lifecycle("\n\n\n=== Building target ${target}, configuration ${configuration}  ===")
        if (target != "Frankified") {
            projectHelper.executeCommand(project, tmpDir(target, configuration), iosConf.getXCodeBuildExecutionPath(target, configuration) + [
                    "-target",
                    target,
                    "-configuration",
                    configuration,
                    "-sdk",
                    iosConf.sdk
            ])
            IOSBuilderInfo bi = buidSingleBuilderInfo(target, configuration, 'iphoneos', project)
            buildListeners.each {
                it.buildDone(project, bi)
            }
        } else {
            projectHelper.executeCommand(project, tmpDir(target, configuration), iosConf.getXCodeBuildExecutionPath(target, configuration) + [
                    "-target",
                    target,
                    "-configuration",
                    configuration,
                    "-sdk",
                    iosConf.simulatorsdk,
                    "-arch",
                    "i386"
            ])
        }
    }

    void buildDebugVariant(Project project, String target) {
        checkVersions()
        def configuration = "Debug"
        logger.lifecycle("\n\n\n=== Building DEBUG target ${target}, configuration ${configuration}  ===")
        if (conf.versionString != null) {
            projectHelper.executeCommand(project, tmpDir(target, configuration),
                    iosConf.getXCodeBuildExecutionPath(target, configuration) + [
                            "-target",
                            target,
                            "-configuration",
                            configuration,
                            "-sdk",
                            iosConf.simulatorsdk
                    ])
            IOSBuilderInfo bi = buidSingleBuilderInfo(target, configuration, 'iphonesimulator', project)
            buildListeners.each {
                it.buildDone(project, bi)
            }
        } else {
            logger.lifecycle("Skipping building debug artifacts -> the build is not versioned")
        }
    }


    IOSBuilderInfo buidSingleBuilderInfo(String target, String configuration, String outputDirPostfix, Project project) {
        IOSBuilderInfo bi = new IOSBuilderInfo(
                id: "${target}-${configuration}",
                target: target,
                configuration: configuration,
                buildDirectory: new File(tmpDir(target, configuration), "/build/${configuration}-${outputDirPostfix}"),
                fullReleaseName: "${target}-${configuration}-${conf.fullVersionString}",
                filePrefix: "${target}-${configuration}-${conf.fullVersionString}",
                mobileprovisionFile: IOSXCodeOutputParser.findMobileProvisionFile(project, target, configuration, true),
                plistFile: new File(tmpDir(target, configuration), PropertyCategory.readProperty(project, IOSProjectProperty.PLIST_FILE)))
        return bi
    }

    public File tmpDir(String target, String configuration) {
        return project.file("../tmp-${project.rootDir.name}-${target}-${configuration}")
    }
}
