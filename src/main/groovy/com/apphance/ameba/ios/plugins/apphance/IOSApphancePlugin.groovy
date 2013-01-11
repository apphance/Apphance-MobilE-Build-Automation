package com.apphance.ameba.ios.plugins.apphance

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.apphance.ApphanceProperty
import com.apphance.ameba.apphance.PrepareApphanceSetupOperation
import com.apphance.ameba.apphance.ShowApphancePropertiesOperation
import com.apphance.ameba.apphance.VerifyApphanceSetupOperation
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.IOSXCodeOutputParser
import com.apphance.ameba.ios.PbxProjectHelper
import com.apphance.ameba.ios.plugins.buildplugin.IOSPlugin
import com.apphance.ameba.ios.plugins.buildplugin.IOSSingleVariantBuilder
import groovy.io.FileType
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Plugin for all apphance-relate IOS tasks.
 *
 */
class IOSApphancePlugin implements Plugin<Project> {

    static Logger logger = Logging.getLogger(IOSApphancePlugin.class)

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

            project.prepareSetup.prepareSetupOperations << new PrepareApphanceSetupOperation()
            project.verifySetup.verifySetupOperations << new VerifyApphanceSetupOperation()
            project.showSetup.showSetupOperations << new ShowApphancePropertiesOperation()
        }
    }

    void preprocessBuildsWithApphance(Project project) {
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
                        projConf.xCodeProjectDirectories[variant], target, configuration, project[ApphanceProperty.APPLICATION_KEY.propertyName])
                copyApphanceFramework(project, builder.tmpDir(target, configuration))
            }
        }
    }

    void replaceLogsWithApphance(Project project, File tmpDir) {
        logger.lifecycle("Replacing NSLog logs with Apphance in ${tmpDir}")
        project.ant.replace(casesensitive: 'true', token: 'NSLog',
                value: 'APHLog', summary: true) {
            fileset(dir: tmpDir) { include(name: '**/*.m') }
        }
    }

    private copyApphanceFramework(Project project, File libsDir) {
        logger.lifecycle("Copying apphance into directory " + libsDir)
        libsDir.mkdirs()
        libsDir.traverse([type: FileType.FILES, maxDepth: ProjectHelper.MAX_RECURSION_LEVEL]) { framework ->
            if (framework == ".*[aA]pphance.*\\.framework") {
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


        def projectApphanceZip = new File(libsDir, "apphance.zip")
        projectApphanceZip.delete()
        def apphanceUrl = new URL("http://dev.polidea.pl/ext/32092342903/latest_ios_apphance_new.zip")

        projectApphanceZip << apphanceUrl.getContent()

        logger.lifecycle("Unpacking file " + projectApphanceZip)
        logger.lifecycle("Exists " + projectApphanceZip.exists())
        def command = ["unzip", "${projectApphanceZip}", "-d", "${libsDir}"]
        projectHelper.executeCommand(project, libsDir, command)
    }

    boolean isApphancePresent(File projectDir) {
        def apphancePresent = false

        projectDir.traverse([type: FileType.DIRECTORIES, maxDepth: ProjectHelper.MAX_RECURSION_LEVEL]) { framework ->
            if (framework =~ ".*[aA]pphance.*\\.framework") {
                apphancePresent = true
            }
        }

        if (apphancePresent) {
            logger.lifecycle("Apphance already in project")
        } else {
            logger.lifecycle("Apphance not in project")
        }
        return apphancePresent
    }

    static public final String DESCRIPTION =
        """This plugins provides automated adding of Apphance libraries to the project.
"""

}
