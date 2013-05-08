package com.apphance.ameba.plugins.ios.buildplugin

import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.plugins.ios.IOSBuilderInfo
import com.apphance.ameba.plugins.ios.IOSProjectConfiguration
import com.apphance.ameba.plugins.ios.IOSXCodeOutputParser
import com.apphance.ameba.plugins.ios.MPParser
import com.apphance.ameba.plugins.project.ProjectConfiguration
import com.apphance.ameba.util.file.FileManager
import com.sun.org.apache.xpath.internal.XPathAPI
import groovy.io.FileType
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.xml.sax.SAXParseException

import static com.apphance.ameba.plugins.ios.buildplugin.IOSConfigurationRetriever.getIosProjectConfiguration
import static org.gradle.api.logging.Logging.getLogger

/**
 * Builds single variant for iOS projects.
 *
 */
class IOSSingleVariantBuilder {

    def l = getLogger(getClass())

    Collection<IOSBuildListener> buildListeners = []
    ProjectConfiguration conf
    IOSProjectConfiguration iosConf
    AntBuilder ant
    Project project
    IOSExecutor iosExecutor
    IOSXCodeOutputParser parser = new IOSXCodeOutputParser()

    IOSSingleVariantBuilder(Project project, IOSExecutor iosExecutor, IOSBuildListener... buildListeners) {
        use(PropertyCategory) {
            this.project = project
            this.conf = project.getProjectConfiguration()
            this.iosConf = getIosProjectConfiguration(project)
            this.ant = project.ant
            this.iosExecutor = iosExecutor
            this.buildListeners.addAll(buildListeners)
        }
    }

    private checkVersions() {
        l.info("Application version: ${conf.versionCode} string: ${conf.versionString}")
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
                l.lifecycle("Adding plist file ${it} to processing list")
                result << it
            }
        }
        result
    }

    Collection<File> findAllSourceFiles(File dir) {
        def result = []
        dir.traverse([type: FileType.FILES, maxDepth: FileManager.MAX_RECURSION_LEVEL]) {
            if ((it.name.endsWith(".m") || it.name.endsWith(".h")) && !it.path.contains("/External/")) {
                l.lifecycle("Adding source file ${it} to processing list")
                result << it
            }
        }
        result
    }


    private void replaceBundleInAllPlists(File dir, String newBundleIdPrefix, String oldBundleIdPrefix) {
        l.lifecycle("Finding all plists and replacing ${oldBundleIdPrefix} with ${newBundleIdPrefix}")
        def plistFiles = findAllPlistFiles(dir)
        plistFiles.each { file ->
            l.lifecycle("Parsing ${file}")
            try {
                def root = MPParser.getParsedPlist(file)
                XPathAPI.selectNodeList(root, '/plist/dict/key[text()="CFBundleIdentifier"]').each {
                    String bundleToReplace = it.nextSibling.nextSibling.textContent
                    if (bundleToReplace.startsWith(oldBundleIdPrefix)) {
                        String newResult = newBundleIdPrefix + bundleToReplace.substring(oldBundleIdPrefix.length())
                        it.nextSibling.nextSibling.textContent = newResult
                        file.write(root as String)
                        l.lifecycle("Replaced the bundleId to ${newResult} from ${bundleToReplace} in ${file}")
                    } else if (bundleToReplace.startsWith(newBundleIdPrefix)) {
                        l.lifecycle("Already replaced the bundleId to ${bundleToReplace} in ${file}")
                    } else {
                        l.warn("The bundle to replace ${bundleToReplace} does not start with expected ${oldBundleIdPrefix} in ${file}. Not replacing !!!!!!!.")
                    }
                }
            } catch (SAXParseException e) {
                l.warn("Error when parsing ${file}: ${e}. Skipping.")
            }
        }
        l.lifecycle("Finished processing all plists")
    }

    private void replaceBundleInAllSourceFiles(File dir, String newBundleIdPrefix, String oldBundleIdPrefix) {
        l.lifecycle("Finding all source files")
        def sourceFiles = findAllSourceFiles(dir)
        String valueToFind = 'bundleWithIdentifier:@"' + oldBundleIdPrefix
        String valueToReplace = 'bundleWithIdentifier:@"' + newBundleIdPrefix
        sourceFiles.each { file ->
            String t = file.text
            if (t.contains(valueToFind)) {
                file.write(t.replace(valueToFind, valueToReplace))
                l.lifecycle("Replaced the ${valueToFind} with ${valueToReplace} in ${file}")
            }
        }
        l.lifecycle("Finished processing all source files")
    }

    void replaceBundleId(File dir, String oldBundleId, String newBundleId, String configuration) {
        replaceBundleInAllPlists(dir, newBundleId, oldBundleId)
        replaceBundleInAllSourceFiles(dir, newBundleId, oldBundleId)
        iosConf.distributionDirectories[configuration] = new File(iosConf.distributionDirectory, newBundleId)
        l.lifecycle("New distribution directory: ${iosConf.distributionDirectories[configuration]}")
        l.lifecycle("Replaced the bundleIdprefix everywhere")
    }

    void buildNormalVariant(Project project, String target, String configuration) {
        checkVersions()
        if (project.hasProperty('ios.bundleId.' + configuration)) {
            String newBundleId = project['ios.bundleId.' + configuration]
            String oldBundleId = MPParser.readBundleIdFromProvisionFile(
                    parser.findMobileProvisionFile(project, target, configuration, false).toURI().toURL());
            replaceBundleId(tmpDir(target, configuration), oldBundleId, newBundleId, configuration)
        }
        l.lifecycle("\n\n\n=== Building target ${target}, configuration ${configuration}  ===")
        if (target != "Frankified") {
            iosExecutor.buildTarget(tmpDir(target, configuration), target, configuration)
            IOSBuilderInfo bi = buildSingleBuilderInfo(target, configuration, 'iphoneos', project)
            buildListeners.each {
                it.buildDone(project, bi)
            }
        } else {
            iosExecutor.buildTarget(tmpDir(target, configuration), target, configuration, iosConf.simulatorSDK, "-arch i386")
        }
    }

    void buildDebugVariant(Project project, String target) {
        checkVersions()
        def configuration = "Debug"
        l.lifecycle("\n\n\n=== Building DEBUG target ${target}, configuration ${configuration}  ===")
        if (conf.versionString != null) {
            iosExecutor.buildTarget(tmpDir(target, configuration), target, configuration, iosConf.simulatorSDK)
            IOSBuilderInfo bi = buildSingleBuilderInfo(target, configuration, 'iphonesimulator', project)
            buildListeners.each {
                it.buildDone(project, bi)
            }
        } else {
            l.lifecycle("Skipping building debug artifacts -> the build is not versioned")
        }
    }

    IOSBuilderInfo buildSingleBuilderInfo(String target, String configuration, String outputDirPostfix, Project project) {
        IOSBuilderInfo bi = new IOSBuilderInfo(
                id: "${target}-${configuration}",
                target: target,
                configuration: configuration,
                buildDirectory: new File(tmpDir(target, configuration), "/build/${configuration}-${outputDirPostfix}"),
                fullReleaseName: "${target}-${configuration}-${conf.fullVersionString}",
                filePrefix: "${target}-${configuration}-${conf.fullVersionString}",
                mobileProvisionFile: parser.findMobileProvisionFile(project, target, configuration, true),
                plistFile: new File(tmpDir(target, configuration), PropertyCategory.readProperty(project, IOSProjectProperty.PLIST_FILE))
        )
        bi
    }

    public File tmpDir(String target, String configuration) {
        project.file("../tmp-${project.rootDir.name}-${target}-${configuration}")
    }
}
