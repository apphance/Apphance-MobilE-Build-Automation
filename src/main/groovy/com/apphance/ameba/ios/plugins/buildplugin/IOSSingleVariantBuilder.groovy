package com.apphance.ameba.ios.plugins.buildplugin

import groovy.util.AntBuilder

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSBuilderInfo
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.IOSXCodeOutputParser

class IOSSingleVariantBuilder {

    static Logger logger = Logging.getLogger(IOSSingleVariantBuilder.class)
    ProjectHelper projectHelper
    static Collection<IOSBuildListener> buildListeners = []
    ProjectConfiguration conf
    IOSProjectConfiguration iosConf
    AntBuilder ant

    IOSSingleVariantBuilder(Project project, AntBuilder ant) {
        use (PropertyCategory) {
            this.projectHelper = new ProjectHelper()
            this.conf = project.getProjectConfiguration()
            this.iosConf = IOSXCodeOutputParser.getIosProjectConfiguration(project)
            this.ant = ant
        }
    }

    void buildRelease(Project project, String target, String configuration) {
        logger.lifecycle( "\n\n\n=== Building target ${target}, configuration ${configuration}  ===")
        if (System.getenv()["SKIP_IOS_BUILDS"] != null) {
            logger.lifecycle ("********************* CAUTION !!!! *********************************")
            logger.lifecycle ("* Skipping iOS builds because SKIP_IOS_BUILDS variable is set  *")
            logger.lifecycle ("* This should never happen on actual jenkins build                 *")
            logger.lifecycle ("* If it does make sure that SKIP_IOS_BUILDS variable is unset    *")
            logger.lifecycle ("********************************************************************")
        } else {
            projectHelper.executeCommand(project, iosConf.getXCodeBuildExecutionPath() + [
                "-target",
                target,
                "-configuration",
                configuration,
                "-sdk",
                iosConf.sdk
            ])
        }
        IOSBuilderInfo bi = buidSingleBuilderInfo(target, configuration, project)
        buildListeners.each {
            it.buildDone(project, bi)
        }
    }

    IOSBuilderInfo buidSingleBuilderInfo(String target, String configuration, Project project) {
        IOSBuilderInfo bi= new IOSBuilderInfo(
                        id : "${target}-${configuration}",
                        target : target,
                        configuration : configuration,
                        buildDirectory : new File(project.file( "build"),"${configuration}-iphoneos"),
                        fullReleaseName : "${target}-${configuration}-${conf.fullVersionString}",
                        filePrefix : "${target}-${configuration}-${conf.fullVersionString}",
                        mobileprovisionFile : IOSXCodeOutputParser.findMobileProvisionFile(project, target, configuration),
                        plistFile : iosConf.plistFile)
        return bi
    }
}
