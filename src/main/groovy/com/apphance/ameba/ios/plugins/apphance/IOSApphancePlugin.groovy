package com.apphance.ameba.ios.plugins.apphance

import groovy.io.FileType;

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

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
import com.apphance.ameba.ios.plugins.buildplugin.IOSSingleVariantBuilder;

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
        use (PropertyCategory) {
            this.projectHelper = new ProjectHelper()
            this.conf = project.getProjectConfiguration()
            this.iosXcodeOutputParser = new IOSXCodeOutputParser()
            this.iosConf = iosXcodeOutputParser.getIosProjectConfiguration(project)
            this.iosSingleVariantBuilder = new IOSSingleVariantBuilder(project, project.ant)
            this.pbxProjectHelper = new PbxProjectHelper()

            def trimmedListOutput = projectHelper.executeCommand(project, ["xcodebuild", "-list"]as String[],false, null, null, 1, true)*.trim()
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
				def id = "${target}-${configuration}".toString()
                if (!iosConf.isBuildExcluded(id)) {
					def noSpaceId = id.replaceAll(' ','_')
					def singleTask = project."build-${noSpaceId}"
                    singleTask.doFirst {
                        if (!isApphancePresent(iosSingleVariantBuilder.tmpDir(target, configuration))) {
//                            replaceLogsWithApphance(project, iosSingleVariantBuilder.tmpDir(target, configuration))
                            pbxProjectHelper.addApphanceToProject(iosSingleVariantBuilder.tmpDir(target, configuration),
                                iosConf.xCodeProjectDirectory, target, configuration, project[ApphanceProperty.APPLICATION_KEY.propertyName])
                            copyApphanceFramework(project, iosSingleVariantBuilder.tmpDir(target, configuration))
                        }
                    }
                }
            }
        }
    }

    void replaceLogsWithApphance(Project project, File tmpDir) {
        logger.lifecycle("Replacing APHLog logs with Apphance in ${tmpDir}")
        project.ant.replace(casesensitive: 'true', token : 'NSLog',
                value: 'APHLog', summary: true) {
                    fileset(dir: tmpDir) { include (name : '**/*.m') }
                }
    }

    private copyApphanceFramework(Project project, File libsDir) {
        logger.lifecycle("Copying apphance into directory " + libsDir)
        libsDir.mkdirs()
        libsDir.traverse([type: FileType.FILES, maxDepth : ProjectHelper.MAX_RECURSION_LEVEL]) { framework ->
            if (framework == ".*[aA]pphance.*\\.framework") {
                logger.lifecycle("Removing old apphance framework: " + framework.name)
                def delClos = {
                    it.eachDir( delClos );
                    it.eachFile {
                        it.delete()
                    }
                }

                // Apply closure
                delClos( new File(framework.canonicalPath) )
            }
        }

        InputStream apphanceZip = this.class.getResourceAsStream("Apphance-iOS.framework.zip")
        def projectApphanceZip = new File(libsDir, "apphance.zip")
        projectApphanceZip.delete()
        projectApphanceZip.withOutputStream { out ->
            out << apphanceZip.getBytes()
        }

        logger.lifecycle("Unpacking file " + projectApphanceZip)
        logger.lifecycle("Exists " + projectApphanceZip.exists())
        def command = ["unzip", "${projectApphanceZip}", "-d", "${libsDir}"]
        projectHelper.executeCommand(project, libsDir, command)
    }

    boolean isApphancePresent(File projectDir) {
        def apphancePresent = false

        projectDir.traverse([type: FileType.DIRECTORIES, maxDepth : ProjectHelper.MAX_RECURSION_LEVEL]) { framework ->
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
