package com.apphance.flow.plugins.ios.apphance

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.executor.command.Command
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.plugins.apphance.ApphancePluginCommons
import com.apphance.flow.plugins.ios.apphance.pbx.IOSApphancePbxEnhancer
import com.apphance.flow.plugins.ios.apphance.pbx.IOSApphancePbxEnhancerFactory
import com.apphance.flow.plugins.ios.apphance.source.IOSApphanceSourceEnhancer
import com.apphance.flow.plugins.ios.apphance.source.IOSApphanceSourceEnhancerFactory
import com.apphance.flow.plugins.ios.parsers.PbxJsonParser
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
    @Inject PbxJsonParser pbxJsonParser
    @Inject IOSApphancePbxEnhancerFactory apphancePbxEnhancerFactory
    @Inject IOSApphanceSourceEnhancerFactory apphanceSourceEnhancerFactory

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
        if (pbxJsonParser.isFrameworkDeclared(variant.variantPbx, APPHANCE_FRAMEWORK_NAME_PATTERN) || findApphanceInPath()) {
            //TODO do not throw exception!!
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
        variant.tmpDir.traverse([type: DIRECTORIES, maxDepth: MAX_RECURSION_LEVEL]) {
            if (it.name =~ APPHANCE_FRAMEWORK_NAME_PATTERN) {
                apphanceFound = true
            }
        }
        apphanceFound
    }

    @PackageScope
    void downloadDependency() {
        def apphanceZip = new File(variant.tmpDir, 'apphance.zip')

        String confName = "apphance$variant.name".toString()

        addApphanceConfiguration(project, confName)
        project.dependencies {
            "apphance$variant.name" apphanceLibDependency
        }

        try {
            downloadApphance(confName, apphanceZip.name)
        } catch (e) {
            logger.error("Error while resolving dependency: ${apphanceLibDependency}, error: $e.message")
            throw new GradleException(format(bundle.getString('exception.apphance.dependency'), apphanceLibDependency, variant.name))
        }
        unzip(apphanceZip)
        checkFrameworkFolders(apphanceLibDependency)
        apphanceZip.delete()
    }

    @Lazy
    @PackageScope
    String apphanceLibDependency = {
        "com.apphance:ios.${apphanceDependencyGroup}.${variant.apphanceDependencyArch()}:${variant.apphanceLibVersion.value}"
    }()

    @Lazy
    @PackageScope
    String apphanceDependencyGroup = {
        libForMode(variant.apphanceMode.value).groupName
    }()

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
        executor.executeCommand(new Command(
                runDir: variant.tmpDir,
                cmd: ['unzip', apphanceZip.canonicalPath, '-d', variant.tmpDir.canonicalPath]))
    }

    @PackageScope
    void checkFrameworkFolders(String dependency) {
        def libVariant = apphanceDependencyGroup.replace('p', 'P')
        def frameworkFolder = new File(variant.tmpDir, "Apphance-${libVariant}.framework")
        if (!frameworkFolder.exists() || !frameworkFolder.isDirectory() || !(frameworkFolder.length() > 0l)) {
            throw new GradleException(format(bundle.getString('exception.apphance.ios.folders'), frameworkFolder.canonicalPath, dependency))
        }
    }
}
