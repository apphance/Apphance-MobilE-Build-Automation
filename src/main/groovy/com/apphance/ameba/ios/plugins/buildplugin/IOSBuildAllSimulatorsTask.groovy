package com.apphance.ameba.ios.plugins.buildplugin

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSBuilderInfo
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.IOSXCodeOutputParser
import com.apphance.ameba.ios.plugins.release.IOSReleaseConfiguration
import com.apphance.ameba.ios.plugins.release.IOSReleaseConfigurationRetriever
import com.apphance.ameba.plugins.release.ProjectReleaseCategory
import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

/**
 * Task to build iOS simulators - executable images that can be run on OSX.
 */
class IOSBuildAllSimulatorsTask extends DefaultTask {

    Logger logger = Logging.getLogger(IOSBuildAllSimulatorsTask.class)
    ProjectHelper projectHelper
    ProjectConfiguration conf
    IOSProjectConfiguration iosConf
    ProjectReleaseConfiguration releaseConf
    IOSReleaseConfiguration iosReleaseConf
    IOSSingleVariantBuilder iosSingleVariantBuilder

    IOSBuildAllSimulatorsTask() {
        this.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
        this.description = 'Builds all simulators for the project'
        this.projectHelper = new ProjectHelper();
        this.conf = PropertyCategory.getProjectConfiguration(project)
        this.releaseConf = ProjectReleaseCategory.getProjectReleaseConfiguration(project)
        this.iosSingleVariantBuilder = new IOSSingleVariantBuilder(project)
        this.dependsOn(project.readProjectConfiguration, project.copyMobileProvision, project.copyDebugSources)
    }

    String getFolderPrefix(IOSBuilderInfo bi) {
        return "${releaseConf.projectDirectoryName}/${conf.fullVersionString}/${bi.target}/${bi.configuration}"
    }


    @TaskAction
    void buildAllSimulators() {
        iosConf = IOSXCodeOutputParser.getIosProjectConfiguration(project)
        iosReleaseConf = IOSReleaseConfigurationRetriever.getIosReleaseConfiguration(project)
        iosConf.targets.each { target ->
            iosSingleVariantBuilder.buildDebugVariant(project, target)
        }
    }
}
