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
import com.google.inject.AbstractModule
import com.google.inject.Guice
import org.gradle.api.Project
import spock.lang.Specification
import spock.lang.Unroll

import static com.apphance.ameba.util.ProjectType.ANDROID
import static com.apphance.ameba.util.ProjectType.iOS

class PluginMasterSpec extends Specification {

    final projectTypeDetectorMock = Mock(ProjectTypeDetector)

    def mockToMap = {
        [
                type: it,
                instance: Mock(it)
        ]
    }
    final commonPlugins = [ProjectConfigurationPlugin, ProjectReleasePlugin].collect(mockToMap)

    def createInjectorForPluginsMocks(mocks) {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ProjectTypeDetector).toInstance(projectTypeDetectorMock)
                mocks.each {
                    bind(it.type).toInstance(it.instance)
                }
            }
        })
    }

    @Unroll
    def 'test if all #type plugins are applied'() {
        given:
        def mocks = plugins.collect(mockToMap) + commonPlugins
        def master = createInjectorForPluginsMocks(mocks).getInstance(PluginMaster)

        and:
        def project = Mock(Project)

        and: 'tell that project is Android'
        projectTypeDetectorMock.detectProjectType(_) >> type

        when:
        master.enhanceProject(project)

        then:
        interaction {
            mocks.each {
                1 * it.instance.apply(project)
            }
        }

        where:
        type    | plugins
        ANDROID | [AndroidPlugin, AndroidAnalysisPlugin, AndroidApphancePlugin,
                AndroidJarLibraryPlugin, AndroidReleasePlugin, AndroidTestPlugin]
        iOS     | [IOSPlugin, IOSFrameworkPlugin, IOSReleasePlugin, IOSApphancePlugin, IOSUnitTestPlugin]
    }
}
