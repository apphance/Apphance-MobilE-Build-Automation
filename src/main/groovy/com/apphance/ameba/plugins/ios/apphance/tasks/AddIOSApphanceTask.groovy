package com.apphance.ameba.plugins.ios.apphance.tasks

import com.apphance.ameba.configuration.apphance.ApphanceConfiguration
import com.apphance.ameba.configuration.ios.AbstractIOSVariant
import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.apphance.ApphancePluginCommons
import com.apphance.ameba.plugins.ios.buildplugin.IOSSingleVariantBuilder
import com.google.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project

import static com.apphance.ameba.plugins.apphance.ApphanceProperty.APPLICATION_KEY
import static com.apphance.ameba.util.file.FileManager.MAX_RECURSION_LEVEL
import static groovy.io.FileType.DIRECTORIES
import static groovy.io.FileType.FILES
import static java.io.File.separator
import static org.gradle.api.logging.Logging.getLogger

@Mixin(ApphancePluginCommons)
class AddIOSApphanceTask extends DefaultTask{

    static final FRAMEWORK_PATTERN = ~/.*[aA]pphance.*\.framework/

    private l = getLogger(getClass())

    private Project project
    @Inject CommandExecutor executor
    @Inject IOSExecutor iosExecutor
//    private IOSProjectConfiguration iosConf
    private PbxProjectHelper pbxProjectHelper

    @Inject ApphanceConfiguration apphanceConf

    private String variant
    private String target
    private String configuration

    AddIOSApphanceTask(Project project, CommandExecutor executor, IOSExecutor iosExecutor, AbstractIOSVariant variantConf) {
        this.project = project
        this.executor = executor
        this.iosExecutor = iosExecutor
        this.pbxProjectHelper = new PbxProjectHelper(project.properties['apphance.lib']?.toString(),
                project.properties['apphance.mode']?.toString())

        this.variant = variantConf.name
        // FIXME this.target = variantConf.
        this.configuration = 'Debug'
    }

    void addIOSApphance() {
        def builder = new IOSSingleVariantBuilder(project, iosExecutor)
        if (!isApphancePresent(builder.tmpDir(target, configuration))) {
            l.lifecycle("Adding Apphance to ${variant} (${target}, ${configuration}): ${builder.tmpDir(target, configuration)}. Project file = ${iosConf.xCodeProjectDirectories[variant]}")
            pbxProjectHelper.addApphanceToProject(builder.tmpDir(target, configuration),
                    iosConf.xCodeProjectDirectories[variant], target, configuration, project[APPLICATION_KEY.propertyName])
            copyApphanceFramework(builder.tmpDir(target, configuration))
        }
    }

    private boolean isApphancePresent(File projectDir) {
        l.lifecycle("Looking for apphance in: ${projectDir.absolutePath}")

        def apphancePresent = false

        projectDir.traverse([type: DIRECTORIES, maxDepth: MAX_RECURSION_LEVEL]) { file ->
            if (file.name =~ FRAMEWORK_PATTERN) {
                apphancePresent = true
            }
        }

        l.lifecycle("Apphance ${apphancePresent ? 'already' : 'not'} in project")

        apphancePresent
    }

    private copyApphanceFramework(File libsDir) {

        def apphanceLibDependency = prepareApphanceLibDependency(project, 'com.apphance:ios.pre-production.armv7:1.8+')

        libsDir.mkdirs()
        clearLibsDir(libsDir)
        l.lifecycle("Copying apphance framework directory " + libsDir)

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
            l.error("""$msg.
To solve the problem add correct dependency to gradle.properties file or add -Dapphance.lib=<apphance.lib> to invocation.
Dependency should be added in gradle style to 'apphance.lib' entry""")
            throw new GradleException(msg)
        }

        def projectApphanceZip = new File(libsDir, "apphance.zip")
        l.lifecycle("Unpacking file " + projectApphanceZip)
        l.lifecycle("Exists " + projectApphanceZip.exists())
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
                l.lifecycle("Removing old apphance framework: " + framework.name)
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
