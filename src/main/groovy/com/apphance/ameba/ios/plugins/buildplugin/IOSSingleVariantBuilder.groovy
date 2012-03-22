package com.apphance.ameba.ios.plugins.buildplugin

import java.io.File;

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
    Project project

    IOSSingleVariantBuilder(Project project, AntBuilder ant) {
        use (PropertyCategory) {
            this.project = project
            this.projectHelper = new ProjectHelper()
            this.conf = project.getProjectConfiguration()
            this.iosConf = IOSXCodeOutputParser.getIosProjectConfiguration(project)
            this.ant = ant
        }
    }

    void buildNormalVariant(Project project, String target, String configuration) {
        logger.lifecycle( "\n\n\n=== Building target ${target}, configuration ${configuration}  ===")
        projectHelper.executeCommand(project,tmpDir(target,configuration), iosConf.getXCodeBuildExecutionPath() + [
            "-target",
            target,
            "-configuration",
            configuration,
            "-sdk",
            iosConf.sdk
        ])
        IOSBuilderInfo bi = buidSingleBuilderInfo(target, configuration, 'iphoneos', project)
        buildListeners.each {
            it.buildDone(project, bi)
        }
    }

    void buildDebugRelease(Project project, String target) {
        def configuration = "Debug"
        logger.lifecycle( "\n\n\n=== Building DEBUG target ${target}, configuration ${configuration}  ===")
        if (conf.versionString != null) {
            projectHelper.executeCommand(project, tmpDir(target,configuration), iosConf.getXCodeBuildExecutionPath() + [
                "-target",
                target,
                "-configuration",
                configuration,
                "-sdk",
                iosConf.simulatorsdk
            ])
            IOSBuilderInfo bi= buidSingleBuilderInfo(target, configuration, 'iphonesimulator', project)
            buildListeners.each {
                it.buildDone(project, bi)
            }
        } else {
            logger.lifecycle("Skipping building debug artifacts -> the build is not versioned")
        }
    }


    IOSBuilderInfo buidSingleBuilderInfo(String target, String configuration, String outputDirPostfix, Project project) {
        IOSBuilderInfo bi= new IOSBuilderInfo(
                        id : "${target}-${configuration}",
                        target : target,
                        configuration : configuration,
                        buildDirectory : new File(tmpDir(target, configuration),"/build/${configuration}-${outputDirPostfix}"),
                        fullReleaseName : "${target}-${configuration}-${conf.fullVersionString}",
                        filePrefix : "${target}-${configuration}-${conf.fullVersionString}",
                        mobileprovisionFile : IOSXCodeOutputParser.findMobileProvisionFile(project, target, configuration),
                        plistFile : iosConf.plistFile)
        return bi
    }

    public File tmpDir(String target, String configuration) {
        return project.file("../tmp-${target}-${configuration}")
    }
}
