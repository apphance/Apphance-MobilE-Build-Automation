package com.apphance.ameba.ios.plugins.apphance

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
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import static com.apphance.ameba.ProjectHelper.MAX_RECURSION_LEVEL
import static com.apphance.ameba.apphance.ApphanceProperty.APPLICATION_KEY
import static groovy.io.FileType.DIRECTORIES
import static groovy.io.FileType.FILES

/**
 * Plugin for all apphance-relate IOS tasks.
 *
 */
class IOSApphancePlugin implements Plugin<Project> {

    static Logger logger = Logging.getLogger(IOSApphancePlugin.class)
    static final String FRAMEWORK_PATTERN = ".*[aA]pphance.*\\.framework"

    ProjectHelper projectHelper
    ProjectConfiguration conf
    IOSProjectConfiguration iosConf
    IOSXCodeOutputParser iosXcodeOutputParser
    PbxProjectHelper pbxProjectHelper
    IOSSingleVariantBuilder iosSingleVariantBuilder

    public void apply(Project project) {
        ProjectHelper.checkAllPluginsAreLoaded(project, this.class, IOSPlugin.class)
        use(PropertyCategory) {
            this.projectHelper = new ProjectHelper()
            this.conf = project.getProjectConfiguration()
            this.iosXcodeOutputParser = new IOSXCodeOutputParser()
            this.iosConf = iosXcodeOutputParser.getIosProjectConfiguration(project)
            this.iosSingleVariantBuilder = new IOSSingleVariantBuilder(project, project.ant)
            this.pbxProjectHelper = new PbxProjectHelper()

            def trimmedListOutput = projectHelper.executeCommand(project, ["xcodebuild", "-list"] as String[], false, null, null, 1, true)*.trim()
            iosConf.configurations = iosXcodeOutputParser.readBuildableConfigurations(trimmedListOutput)
            iosConf.targets = iosXcodeOutputParser.readBuildableTargets(trimmedListOutput)
            preprocessBuildsWithApphance(project)
            prepareApphanceFrameworkVersion(project)

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
            maven { url 'https://dev.polidea.pl/artifactory/ext-releases-local/' }
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
        project.dependencies { apphance 'com.apphance:ios.pre-production.armv7:1.8+' }
        libsDir.mkdirs()
        clearLibsDir(libsDir)
        logger.lifecycle("Copying apphance framework directory " + libsDir)

        project.copy {
            from { project.configurations.apphance }
            into libsDir
            rename { String filename ->
                'apphance.zip'
            }
        }

        def projectApphanceZip = new File(libsDir, "apphance.zip")
        logger.lifecycle("Unpacking file " + projectApphanceZip)
        logger.lifecycle("Exists " + projectApphanceZip.exists())
        def command = ["unzip", "${projectApphanceZip}", "-d", "${libsDir}"]
        projectHelper.executeCommand(project, libsDir, command)

        project.delete {
            projectApphanceZip
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
