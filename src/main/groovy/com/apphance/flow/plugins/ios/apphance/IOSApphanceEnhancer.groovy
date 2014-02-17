package com.apphance.flow.plugins.ios.apphance

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.executor.command.Command
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.plugins.ios.apphance.pbx.IOSApphancePbxEnhancer
import com.apphance.flow.plugins.ios.apphance.pbx.IOSApphancePbxEnhancerFactory
import com.apphance.flow.plugins.ios.apphance.source.IOSApphanceSourceEnhancer
import com.apphance.flow.plugins.ios.apphance.source.IOSApphanceSourceEnhancerFactory
import com.apphance.flow.plugins.ios.parsers.PbxJsonParser
import com.apphance.flow.util.FlowUtils
import com.google.inject.Inject
import com.google.inject.assistedinject.Assisted
import groovy.transform.PackageScope
import org.gradle.api.GradleException

import static com.apphance.flow.configuration.apphance.ApphanceArtifactory.IOS_APPHANCE_REPO
import static com.apphance.flow.configuration.apphance.ApphanceLibType.libForMode
import static com.apphance.flow.util.file.FileManager.MAX_RECURSION_LEVEL
import static groovy.io.FileType.DIRECTORIES
import static java.text.MessageFormat.format
import static java.util.ResourceBundle.getBundle
import static org.gradle.api.logging.Logging.getLogger

class IOSApphanceEnhancer {

    static final APPHANCE_FRAMEWORK_NAME_PATTERN = ~/.*[aA]pphance.*\.framework/

    private logger = getLogger(getClass())

    @Inject CommandExecutor executor
    @Inject PbxJsonParser pbxJsonParser
    @Inject IOSApphancePbxEnhancerFactory apphancePbxEnhancerFactory
    @Inject IOSApphanceSourceEnhancerFactory apphanceSourceEnhancerFactory
    @Inject FlowUtils flowUtils

    private AbstractIOSVariant variant
    private bundle = getBundle('validation')
    @Lazy
    private IOSApphancePbxEnhancer apphancePbxEnhancer = {
        apphancePbxEnhancerFactory.create(variant)
    }()
    @Lazy
    private IOSApphanceSourceEnhancer apphanceSourceEnhancer = {
        apphanceSourceEnhancerFactory.create(variant, apphancePbxEnhancer)
    }()

    @Inject
    IOSApphanceEnhancer(@Assisted AbstractIOSVariant variant) {
        this.variant = variant
    }

    void enhanceApphance() {
        logger.info("Adding apphance for variant '$variant.name'")
        if (pbxJsonParser.isFrameworkDeclared(variant.pbxFile, APPHANCE_FRAMEWORK_NAME_PATTERN) || findApphanceInPath()) {
            logger.warn("\n\nApphance framework found for variant: $variant.name in dir: ${variant.tmpDir.absolutePath}. Apphance will not be added!!\n\n")
        } else {
            apphancePbxEnhancer.addApphanceToPbx()
            apphanceSourceEnhancer.addApphanceToSource()
            downloadDependency()
        }
    }

    @PackageScope
    boolean findApphanceInPath() {
        logger.info("Searching for apphance in: $variant.tmpDir.absolutePath")

        def apphanceFound = false
        variant.tmpDir.traverse([type: DIRECTORIES, maxDepth: MAX_RECURSION_LEVEL]) {
            if (it.name =~ APPHANCE_FRAMEWORK_NAME_PATTERN) {
                apphanceFound = true
            }
        }
        apphanceFound
    }

    @PackageScope
    void downloadDependency() {
        try {
            def apphanceZip = downloadApphance(apphanceUrl)
            unzip(apphanceZip)
            apphanceZip.delete()
        } catch (e) {
            logger.error("Error while resolving dependency: '$apphanceUrl', error: $e.message")
            throw new GradleException(format(bundle.getString('exception.apphance.dependency'), apphanceUrl, variant.name))
        }
        checkFrameworkFolders()
    }

    @Lazy
    @PackageScope
    String apphanceUrl = {
        def suffix = "apphance-$variant.aphMode.value.repoSuffix"
        def lib = variant.aphLib.value
        variant.aphLibURL.hasValue() ? variant.aphLibURL.value : "$IOS_APPHANCE_REPO/com/utest/$suffix/$lib/$suffix-${lib}.zip"
    }()

    @Lazy
    @PackageScope
    String apphanceDependencyGroup = {
        libForMode(variant.aphMode.value).groupName
    }()

    private File downloadApphance(String apphanceURL) {
        flowUtils.downloadToTempFile(apphanceURL)
    }

    private void unzip(File apphanceZip) {
        executor.executeCommand(new Command(
                runDir: variant.tmpDir,
                cmd: ['unzip', apphanceZip.canonicalPath, '-d', variant.tmpDir.canonicalPath]))
    }

    @PackageScope
    void checkFrameworkFolders() {
        def libVariant = apphanceDependencyGroup.replace('p', 'P')
        def frameworkFolder = new File(variant.tmpDir, "Apphance-${libVariant}.framework")
        if (!frameworkFolder.exists() || !frameworkFolder.isDirectory() || !(frameworkFolder.length() > 0l)) {
            throw new GradleException(format(bundle.getString('exception.apphance.ios.folders'), frameworkFolder.canonicalPath, variant.aphLib))
        }
    }
}
