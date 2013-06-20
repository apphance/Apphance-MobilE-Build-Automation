package com.apphance.flow.plugins.ios.apphance

import com.apphance.flow.configuration.apphance.ApphanceArtifactory
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.plugins.apphance.ApphancePluginCommons
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
import org.gradle.api.Project

import static com.apphance.flow.configuration.apphance.ApphanceLibType.libForMode
import static com.apphance.flow.util.file.FileManager.MAX_RECURSION_LEVEL
import static groovy.io.FileType.DIRECTORIES
import static java.text.MessageFormat.format
import static java.util.ResourceBundle.getBundle
import static org.gradle.api.logging.Logging.getLogger

@Mixin(ApphancePluginCommons)
class IOSApphanceEnhancer {

    static final APPHANCE_FRAMEWORK_NAME_PATTERN = ~/.*[aA]pphance.*\.framework/

    private logger = getLogger(getClass())

    @Inject Project project
    @Inject CommandExecutor executor
    @Inject IOSExecutor iosExecutor
    @Inject PbxJsonParser pbxJsonParser
    @Inject IOSApphancePbxEnhancerFactory apphancePbxEnhancerFactory
    @Inject IOSApphanceSourceEnhancerFactory apphanceSourceEnhancerFactory
    @Inject FlowUtils flowUtils
    @Inject ApphanceArtifactory apphanceArtifactory

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

    void addApphance() {
        if (pbxJsonParser.isFrameworkDeclared(APPHANCE_FRAMEWORK_NAME_PATTERN) || findApphanceInPath()) {
            throw new GradleException(format(bundle.getString('exception.apphance.declared'), variant.name, variant.tmpDir.absolutePath))
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
        variant.tmpDir.traverse([type: DIRECTORIES, maxDepth: MAX_RECURSION_LEVEL]) { file ->
            if (file.name =~ APPHANCE_FRAMEWORK_NAME_PATTERN) {
                apphanceFound = true
            }
        }
        apphanceFound
    }

    @PackageScope
    void downloadDependency() {
        def dependency = apphanceLibDependency()
        def apphanceFileName = 'apphance.zip'
        def apphanceZip = new File(variant.tmpDir, apphanceFileName)

        String confName = "apphance$variant.name".toString()

        addApphanceConfiguration(project, confName)
        project.dependencies {
            "apphance$variant.name" dependency
        }

        try {
            downloadApphance(confName, apphanceFileName)
        } catch (e) {
            logger.error("Error while resolving dependency: $dependency, error: $e.message")
            throw new GradleException(format(bundle.getString('exception.apphance.dependency'), dependency, variant.name))
        }
        unzip(apphanceZip)
        checkFrameworkFolders(dependency)
        apphanceZip.delete()
    }

    @PackageScope
    String apphanceLibDependency() {
        "com.apphance:ios.${apphanceDependencyGroup()}.${apphanceDependencyArch()}:${variant.apphanceLibVersion.value}"
    }

    @PackageScope
    String apphanceDependencyGroup() {
        libForMode(variant.apphanceMode.value).groupName
    }

    @PackageScope
    String apphanceDependencyArch() {
        def xc = availableXCodeArchitectures()
        def af = apphanceArtifactory.iOSArchs(variant.apphanceMode.value)
        af.retainAll(xc)
        af.unique().sort()[-1]
    }

    @PackageScope
    Collection<String> availableXCodeArchitectures() {
        iosExecutor.buildSettings(variant.target, variant.configuration)['ARCHS'].split(' ')*.trim()
    }

    private void downloadApphance(String confName, String apphanceFileName) {
        project.copy {
            from { project.configurations.getByName(confName) }
            into variant.tmpDir
            rename { String filename ->
                apphanceFileName
            }
        }
    }

    private void unzip(File apphanceZip) {
        flowUtils.unzip(apphanceZip, variant.tmpDir)
    }

    @PackageScope
    void checkFrameworkFolders(String dependency) {
        def libVariant = apphanceDependencyGroup().replace('p', 'P')
        def frameworkFolder = "Apphance-${libVariant}.framework"
        def frameworkFolderFile = new File(variant.tmpDir, frameworkFolder)
        if (!frameworkFolderFile.exists() || !frameworkFolderFile.isDirectory() || !(frameworkFolderFile.length() > 0l)) {
            throw new GradleException(format(bundle.getString('exception.apphance.ios.folders'), frameworkFolderFile.canonicalPath, dependency))
        }
    }
}
