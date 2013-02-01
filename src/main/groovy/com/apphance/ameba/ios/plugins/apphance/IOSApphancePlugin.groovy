package com.apphance.ameba.ios.plugins.apphance

import com.apphance.ameba.PluginHelper
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.apphance.PrepareApphanceSetupOperation
import com.apphance.ameba.apphance.ShowApphancePropertiesOperation
import com.apphance.ameba.apphance.VerifyApphanceSetupOperation
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.IOSXCodeOutputParser
import com.apphance.ameba.ios.PbxProjectHelper
import com.apphance.ameba.ios.plugins.buildplugin.IOSPlugin
import com.apphance.ameba.ios.plugins.buildplugin.IOSSingleVariantBuilder
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import static com.apphance.ameba.util.file.FileManager.MAX_RECURSION_LEVEL
import static com.apphance.ameba.apphance.ApphanceProperty.APPLICATION_KEY
import static groovy.io.FileType.DIRECTORIES
import static groovy.io.FileType.FILES
import static java.io.File.separator

/**
 * Plugin for all apphance-relate IOS tasks.
 *
 */
class IOSApphancePlugin implements Plugin<Project> {

    static Logger logger = Logging.getLogger(IOSApphancePlugin.class)
    static final FRAMEWORK_PATTERN = ~/.*[aA]pphance.*\.framework/

    ProjectHelper projectHelper
    ProjectConfiguration conf
    IOSProjectConfiguration iosConf
    IOSXCodeOutputParser iosXcodeOutputParser
    PbxProjectHelper pbxProjectHelper
    IOSSingleVariantBuilder iosSingleVariantBuilder

    public void apply(Project project) {
        PluginHelper.checkAllPluginsAreLoaded(project, this.class, IOSPlugin.class)
        use(PropertyCategory) {
            this.projectHelper = new ProjectHelper()
            this.conf = project.getProjectConfiguration()
            this.iosXcodeOutputParser = new IOSXCodeOutputParser()
            this.iosConf = iosXcodeOutputParser.getIosProjectConfiguration(project)
            this.iosSingleVariantBuilder = new IOSSingleVariantBuilder(project, project.ant)
            this.pbxProjectHelper = new PbxProjectHelper(project.properties['apphance.lib'])

            def trimmedListOutput = projectHelper.executeCommand(project, ["xcodebuild", "-list"] as String[], false, null, null, 1, true)*.trim()
            iosConf.configurations = iosXcodeOutputParser.readBuildableConfigurations(trimmedListOutput)
            iosConf.targets = iosXcodeOutputParser.readBuildableTargets(trimmedListOutput)
            prepareApphanceFrameworkVersion(project)
            preprocessBuildsWithApphance(project)

            project.prepareSetup.prepareSetupOperations << new PrepareApphanceSetupOperation()
            project.verifySetup.verifySetupOperations << new VerifyApphanceSetupOperation()
            project.showSetup.showSetupOperations << new ShowApphancePropertiesOperation()
        }
    }

    private void prepareApphanceFrameworkVersion(Project project) {
        project.configurations {
            apphance
        }

        project.configurations.apphance {
            resolutionStrategy.cacheDynamicVersionsFor 0, 'minutes'
        }

        project.repositories {
            project.repositories {
                maven { url 'https://dev.polidea.pl/artifactory/libs-releases-local/' }
                maven { url 'https://dev.polidea.pl/artifactory/libs-snapshots-local/' }
            }
        }
    }

    private void preprocessBuildsWithApphance(Project project) {
        iosConf.configurations.each { configuration ->
            iosConf.targets.each { target ->
                def variant = "${target}-${configuration}".toString()
                if (!iosConf.isBuildExcluded(variant)) {
                    def noSpaceId = variant.replaceAll(' ', '_')
                    def singleTask = project."build-${noSpaceId}"
                    addApphanceToTask(project, singleTask, variant, target, configuration, iosConf)
                }
            }
        }
        if (project.hasProperty('buildAllSimulators')) {
            addApphanceToTask(project, project.buildAllSimulators, "${this.iosConf.mainTarget}-Debug", this.iosConf.mainTarget, 'Debug', iosConf)
        }
    }

    private addApphanceToTask(Project project, singleTask, String variant, String target, String configuration, IOSProjectConfiguration projConf) {
        singleTask.doFirst {
            def builder = new IOSSingleVariantBuilder(project, new AntBuilder())
            if (!isApphancePresent(builder.tmpDir(target, configuration))) {
                logger.info("Adding Apphance to ${variant} (${target}, ${configuration}): " +
                        "${builder.tmpDir(target, configuration)}. Project file = ${projConf.xCodeProjectDirectories[variant]}")
                pbxProjectHelper.addApphanceToProject(builder.tmpDir(target, configuration),
                        projConf.xCodeProjectDirectories[variant], target, configuration, project[APPLICATION_KEY.propertyName])
                copyApphanceFramework(project, builder.tmpDir(target, configuration))
            }
        }
    }

    private copyApphanceFramework(Project project, File libsDir) {

        def apphanceLibDependency = prepareApphanceLibDependency(project)

        libsDir.mkdirs()
        clearLibsDir(libsDir)
        logger.lifecycle("Copying apphance framework directory " + libsDir)

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
            logger.error("""$msg.
To solve the problem add correct dependency to gradle.properties file or add -Dapphance.lib=<apphance.lib> to invocation.
Dependency should be added in gradle style to 'apphance.lib' entry""")
            throw new GradleException(msg)
        }

        def projectApphanceZip = new File(libsDir, "apphance.zip")
        logger.lifecycle("Unpacking file " + projectApphanceZip)
        logger.lifecycle("Exists " + projectApphanceZip.exists())
        def command = ["unzip", "${projectApphanceZip}", "-d", "${libsDir}"]
        projectHelper.executeCommand(project, libsDir, command)

        checkFrameworkFolders(apphanceLibDependency, libsDir)

        project.delete {
            projectApphanceZip
        }
    }

    private String prepareApphanceLibDependency(Project p) {
        def apphanceLibDependency

        use(PropertyCategory) {
            apphanceLibDependency = p.readPropertyOrEnvironmentVariable('apphance.lib', true)
            apphanceLibDependency = apphanceLibDependency ? apphanceLibDependency : 'com.apphance:ios.pre-production.armv7:1.8+'
            if (p.configurations.apphance.dependencies.isEmpty())
                p.dependencies { apphance apphanceLibDependency }
        }
        apphanceLibDependency
    }

    private void checkFrameworkFolders(String apphanceLib, File libsDir) {
        def libVariant = apphanceLib.split(':')[1].split('\\.')[1].replace('p', 'P')
        def frameworkFolder = "Apphance-${libVariant}.framework"
        def frameworkFolderFile = new File(libsDir.canonicalPath + separator + frameworkFolder)
        if (!frameworkFolderFile.exists() || !frameworkFolderFile.isDirectory() || !(frameworkFolderFile.length() > 0l)) {
            throw new GradleException("There is no framework folder (or may be empty): ${frameworkFolderFile.canonicalPath} associated with apphance version: '${apphanceLib}'")
        }
    }

    private clearLibsDir(File libsDir) {
        libsDir.traverse([type: FILES, maxDepth: MAX_RECURSION_LEVEL]) { framework ->
            if (framework.name =~ FRAMEWORK_PATTERN) {
                logger.lifecycle("Removing old apphance framework: " + framework.name)
                def delClos = {
                    it.eachDir(delClos);
                    it.eachFile {
                        it.delete()
                    }
                }

                // Apply closure
                delClos(new File(framework.canonicalPath))
            }
        }
    }

    boolean isApphancePresent(File projectDir) {
        def apphancePresent = false

        projectDir.traverse([type: DIRECTORIES, maxDepth: MAX_RECURSION_LEVEL]) { framework ->
            if (framework =~ FRAMEWORK_PATTERN) {
                apphancePresent = true
            }
        }

        apphancePresent ?
            logger.lifecycle("Apphance already in project") :
            logger.lifecycle("Apphance not in project")

        apphancePresent
    }

    static public final String DESCRIPTION =
        """This plugins provides automated adding of Apphance libraries to the project.
"""

}
