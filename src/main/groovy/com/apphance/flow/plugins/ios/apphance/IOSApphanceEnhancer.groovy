package com.apphance.flow.plugins.ios.apphance

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.executor.IOSExecutor
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
import groovy.json.JsonSlurper
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

        project.dependencies {
            apphance dependency
        }

        try {
            downloadApphance(apphanceFileName)
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
        def af = availableArtifactoryArchitectures()
        af.retainAll(xc)
        af.unique().sort()[-1]
    }

    @PackageScope
    Collection<String> availableXCodeArchitectures() {
        iosExecutor.buildSettings(variant.target, variant.configuration)['ARCHS'].split(' ')*.trim()
    }

    @PackageScope
    Collection<String> availableArtifactoryArchitectures() {
        def text = 'https://dev.polidea.pl/artifactory/api/storage/libs-releases-local/com/apphance'.toURL().openStream().readLines().join('\n')
        def json = new JsonSlurper().parseText(text)

        json.children.findAll {
            it.uri.startsWith("/ios.${apphanceDependencyGroup()}")
        }*.uri.collect {
            it.split('\\.')[2]
        }*.trim().unique().sort()
    }

    private void downloadApphance(String apphanceFileName) {
        project.copy {
            from { project.configurations.apphance }
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
        def libVariant = apphanceDependencyGroup().replace('p', 'P')
        def frameworkFolder = "Apphance-${libVariant}.framework"
        def frameworkFolderFile = new File(variant.tmpDir, frameworkFolder)
        if (!frameworkFolderFile.exists() || !frameworkFolderFile.isDirectory() || !(frameworkFolderFile.length() > 0l)) {
            throw new GradleException(format(bundle.getString('exception.apphance.ios.folders'), frameworkFolderFile.canonicalPath, dependency))
        }
    }
}
