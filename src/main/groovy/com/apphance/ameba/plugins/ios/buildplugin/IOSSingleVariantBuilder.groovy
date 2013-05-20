package com.apphance.ameba.plugins.ios.buildplugin

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant
import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.ios.builder.IOSArtifactProvider
import com.apphance.ameba.plugins.ios.builder.IOSBuilderInfo
import com.apphance.ameba.plugins.ios.parsers.MobileProvisionParser
import com.apphance.ameba.plugins.ios.parsers.PlistParser
import com.apphance.ameba.plugins.ios.release.IOSReleaseListener
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.util.file.FileManager.MAX_RECURSION_LEVEL
import static groovy.io.FileType.FILES
import static org.gradle.api.logging.Logging.getLogger

/**
 * Builds single variant for iOS projects.
 *
 */
class IOSSingleVariantBuilder {

    def l = getLogger(getClass())

    Collection<IOSBuildListener> buildListeners = []

    @Inject
    IOSConfiguration conf
    @Inject
    IOSExecutor iosExecutor
    @Inject
    CommandExecutor executor
    @Inject
    IOSArtifactProvider artifactProvider
    @Inject
    MobileProvisionParser mpParser
    @Inject
    PlistParser plistParser

    void registerListener(IOSReleaseListener listener) {
        buildListeners << listener
    }

    private void replaceBundleId(File dir, String oldBundleId, String newBundleId, String configuration) {
        replaceBundleInAllPlists(dir, newBundleId, oldBundleId)
        replaceBundleInAllSourceFiles(dir, newBundleId, oldBundleId)
//        iosConf.distributionDirectories[configuration] = new File(iosConf.distributionDirectory, newBundleId)
//        l.lifecycle("New distribution directory: ${iosConf.distributionDirectories[configuration]}")
        l.lifecycle("Replaced the bundleIdprefix everywhere")
    }

    private void replaceBundleInAllPlists(File dir, String newBundleIdPrefix, String oldBundleIdPrefix) {
        l.lifecycle("Finding all plists and replacing ${oldBundleIdPrefix} with ${newBundleIdPrefix}")
        def plistFiles = findAllPlistFiles(dir)
        plistFiles.each { file ->
            l.lifecycle("Parsing ${file}")
            try {
                plistParser.replaceBundledId(file, oldBundleIdPrefix, newBundleIdPrefix)
            } catch (Exception e) {
                l.warn("Error when parsing ${file}: ${e}. Skipping.")
            }
        }
        l.lifecycle("Finished processing all plists")
    }

    private Collection<File> findAllPlistFiles(File dir) {
        def result = []
        dir.traverse([type: FILES, maxDepth: MAX_RECURSION_LEVEL]) {
            if (it.name.endsWith(".plist") && !it.path.contains("/External/") && !it.path.contains('/build/')) {
                l.lifecycle("Adding plist file ${it} to processing list")
                result << it
            }
        }
        result
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

    private Collection<File> findAllSourceFiles(File dir) {
        def result = []
        dir.traverse([type: FILES, maxDepth: MAX_RECURSION_LEVEL]) {
            if ((it.name.endsWith(".m") || it.name.endsWith(".h")) && !it.path.contains("/External/")) {
                l.lifecycle("Adding source file ${it} to processing list")
                result << it
            }
        }
        result
    }

    //TODO to remove when buildVariant is fully implemented
    void buildNormalVariant(Project project, String target, String configuration) {
        checkVersions()
        if (project.hasProperty('ios.bundleId.' + configuration)) {
            String newBundleId = project['ios.bundleId.' + configuration]
            String oldBundleId = mpParser.bundleId(null)//TODO mobileprovision from variant
            replaceBundleId(tmpDir(target, configuration), oldBundleId, newBundleId, configuration)
        }
        l.lifecycle("\n\n\n=== Building target ${target}, configuration ${configuration}  ===")
        if (target != "Frankified") {
            iosExecutor.buildTarget(tmpDir(target, configuration), target, configuration)
//            IOSBuilderInfo bi = buildSingleBuilderInfo(target, configuration, 'iphoneos', project)
            IOSBuilderInfo bi = artifactProvider.builderInfo(null)//TODO pass variant here
            buildListeners.each {
                it.buildDone(bi)
            }
        } else {
            iosExecutor.buildTarget(tmpDir(target, configuration), target, configuration, conf.simulatorSdk.value, "-arch i386")
        }
    }

    //this method is replacement for buildNormalVariant method above
    void buildVariant(AbstractIOSVariant variant) {
        //TODO replace ios bundleId - how? when?
        //TODO target frankified - what's going on?
        executor.executeCommand(new Command(runDir: variant.tmpDir, cmd: variant.buildCmd()))
        buildListeners.each {
            it.buildDone(artifactProvider.builderInfo(variant))
        }
    }

    void buildDebugVariant(String target) {
        checkVersions()
        def configuration = "Debug"
        l.lifecycle("\n\n\n=== Building DEBUG target ${target}, configuration ${configuration}  ===")
//        if (conf.versionString != null) {
        iosExecutor.buildTarget(tmpDir(target, configuration), target, configuration, conf.simulatorSdk.value)
//            IOSBuilderInfo bi = buildSingleBuilderInfo(target, configuration, 'iphonesimulator', project)
        IOSBuilderInfo bi = artifactProvider.builderInfo(null)//TODO pass variant here
        buildListeners.each {
            it.buildDone(bi)
        }
//        } else {
//            l.lifecycle("Skipping building debug artifacts -> the build is not versioned")
//        }
    }

    private void checkVersions() {
//        l.info("Application version: ${conf.versionCode} string: ${conf.versionString}")
//        if (conf.versionCode == 0) {
//            throw new GradleException("The CFBundleVersion key is missing from ${iosConf.plistFile} or its value is 0. Please add it or increase the value. Integers are only valid values")
//    }
//        if (conf.versionString.startsWith('NOVERSION')) {
//            throw new GradleException("The CFBundleShortVersionString key is missing from ${iosConf.plistFile}. Please add it.")
//}
    }

    public File tmpDir(String target, String configuration) {
//        project.file("../tmp-${project.rootDir.name}-${target}-${configuration}")
        //TODO
        null
    }

}
