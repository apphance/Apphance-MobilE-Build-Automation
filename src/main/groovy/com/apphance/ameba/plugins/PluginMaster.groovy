package com.apphance.ameba.plugins

import com.apphance.ameba.android.plugins.analysis.AndroidAnalysisPlugin
import com.apphance.ameba.android.plugins.apphance.AndroidApphancePlugin
import com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin
import com.apphance.ameba.android.plugins.jarlibrary.AndroidJarLibraryPlugin
import com.apphance.ameba.android.plugins.release.AndroidReleasePlugin
import com.apphance.ameba.android.plugins.test.AndroidTestPlugin
import com.apphance.ameba.detection.ProjectTypeDetector
import com.apphance.ameba.ios.plugins.apphance.IOSApphancePlugin
import com.apphance.ameba.ios.plugins.buildplugin.IOSPlugin
import com.apphance.ameba.ios.plugins.framework.IOSFrameworkPlugin
import com.apphance.ameba.ios.plugins.ocunit.IOSUnitTestPlugin
import com.apphance.ameba.ios.plugins.release.IOSReleasePlugin
import com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin
import com.apphance.ameba.plugins.release.ProjectReleasePlugin
import com.apphance.ameba.util.ProjectType
import com.google.inject.Injector
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.util.ProjectType.ANDROID
import static com.apphance.ameba.util.ProjectType.iOS

class PluginMaster {

    @Inject ProjectTypeDetector projectTypeDetector

    @Inject Injector injector

    static plugins = [
            commons: [
                    ProjectConfigurationPlugin,
                    ProjectReleasePlugin,
            ],

            (iOS) : [
                    IOSPlugin,
                    IOSFrameworkPlugin,
                    IOSReleasePlugin,
                    IOSApphancePlugin,
                    IOSUnitTestPlugin,
            ],

            (ANDROID) : [
                    AndroidPlugin,
                    AndroidAnalysisPlugin,
                    AndroidApphancePlugin,
                    AndroidJarLibraryPlugin,
                    AndroidReleasePlugin,
                    AndroidTestPlugin,
            ],
    ]

    void enhanceProject(Project project) {
        ProjectType projectType = projectTypeDetector.detectProjectType(project.rootDir)

        def installPlugin = {injector.getInstance(it).apply(project)}

        plugins.commons.each installPlugin
        plugins[projectType].each installPlugin
    }
}
