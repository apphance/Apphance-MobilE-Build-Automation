package com.apphance.ameba.plugins.ios.apphance.tasks

import com.apphance.ameba.configuration.apphance.ApphanceConfiguration
import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant
import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.apphance.ApphancePluginCommons
import com.apphance.ameba.plugins.ios.buildplugin.IOSSingleVariantBuilder
import com.google.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException

import static com.apphance.ameba.util.file.FileManager.MAX_RECURSION_LEVEL
import static groovy.io.FileType.DIRECTORIES
import static groovy.io.FileType.FILES
import static java.io.File.separator
import static org.gradle.api.logging.Logging.getLogger

@Mixin(ApphancePluginCommons)
class AddIOSApphanceTask extends DefaultTask {

    static final FRAMEWORK_PATTERN = ~/.*[aA]pphance.*\.framework/

    private log = getLogger(getClass())

    @Inject CommandExecutor executor
    @Inject IOSExecutor iosExecutor
    @Inject ApphanceConfiguration apphanceConf
    @Inject IOSConfiguration iosConfiguration

    //TODO PbxProjectHelper should be injected
    //TODO remove unused fields
    //TODO this class should be written as a service

    private PbxProjectHelper pbxProjectHelper
    private AbstractIOSVariant variant
    private String target
    private String configuration

    AddIOSApphanceTask(AbstractIOSVariant variantConf) {
        this.pbxProjectHelper = new PbxProjectHelper(variantConf.apphanceLibVersion.value, variantConf.apphanceMode.value.toString())

        this.variant = variantConf
        this.target = variantConf.target
        this.configuration = variantConf.target
    }

    void addIOSApphance() {
        def builder = new IOSSingleVariantBuilder(project, iosExecutor)
        if (!isApphancePresent(builder.tmpDir(target, configuration))) {
            log.lifecycle("Adding Apphance to ${variant} (${target}, ${configuration}): ${builder.tmpDir(target, configuration)}. Project file = ${variant.tmpDir}")
            pbxProjectHelper.addApphanceToProject(
                    builder.tmpDir(target, configuration),
                    iosConfiguration.xcodeDir.value,
                    target,
                    configuration,
                    variant.apphanceAppKey.value)
            copyApphanceFramework(builder.tmpDir(target, configuration))
        }
    }

    private boolean isApphancePresent(File projectDir) {
        log.lifecycle("Looking for apphance in: ${projectDir.absolutePath}")

        def apphancePresent = false

        projectDir.traverse([type: DIRECTORIES, maxDepth: MAX_RECURSION_LEVEL]) { file ->
            if (file.name =~ FRAMEWORK_PATTERN) {
                apphancePresent = true
            }
        }

        log.lifecycle("Apphance ${apphancePresent ? 'already' : 'not'} in project")

        apphancePresent
    }

    private copyApphanceFramework(File libsDir) {

        def apphanceLibDependency = prepareApphanceLibDependency(project, 'com.apphance:ios.pre-production.armv7:1.8+')

        libsDir.mkdirs()
        clearLibsDir(libsDir)
        log.lifecycle("Copying apphance framework directory " + libsDir)

        try {
            project.copy {
                from { project.configurations.apphance }
                into libsDir
                rename { String filename ->
                    'apphance.zip'
                }
            }
        } catch (e) {
            def msg = "Error while resolving dependency: '$apphanceLibDependency'"
            log.error("$msg.\nTo solve the problem add correct dependency to gradle.properties file or add -Dapphance.lib=<apphance.lib> to invocation.\n" +
                    "Dependency should be added in gradle style to 'apphance.lib' entry")
            throw new GradleException(msg)
        }

        def projectApphanceZip = new File(libsDir, "apphance.zip")
        log.lifecycle("Unpacking file " + projectApphanceZip)
        log.lifecycle("Exists " + projectApphanceZip.exists())
        executor.executeCommand(new Command(runDir: project.rootDir,
                cmd: ['unzip', projectApphanceZip.canonicalPath, '-d', libsDir.canonicalPath]))

        checkFrameworkFolders(apphanceLibDependency, libsDir)

        project.delete {
            projectApphanceZip
        }
    }

    private clearLibsDir(File libsDir) {
        libsDir.traverse([type: FILES, maxDepth: MAX_RECURSION_LEVEL]) { framework ->
            if (framework.name =~ FRAMEWORK_PATTERN) {
                log.lifecycle("Removing old apphance framework: " + framework.name)
                delClos(new File(framework.canonicalPath))
            }
        }
    }

    private delClos = {
        it.eachDir(delClos);
        it.eachFile {
            it.delete()
        }
    }

    private void checkFrameworkFolders(String apphanceLib, File libsDir) {
        def libVariant = apphanceLib.split(':')[1].split('\\.')[1].replace('p', 'P')
        def frameworkFolder = "Apphance-${libVariant}.framework"
        def frameworkFolderFile = new File(libsDir.canonicalPath + separator + frameworkFolder)
        if (!frameworkFolderFile.exists() || !frameworkFolderFile.isDirectory() || !(frameworkFolderFile.length() > 0l)) {
            throw new GradleException("There is no framework folder (or may be empty): ${frameworkFolderFile.canonicalPath} associated with apphance version: '${apphanceLib}'")
        }
    }
}
