package com.apphance.flow.plugins

import com.apphance.flow.detection.project.ProjectTypeDetector
import com.apphance.flow.di.*
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.android.analysis.AndroidAnalysisPlugin
import com.apphance.flow.plugins.android.apphance.AndroidApphancePlugin
import com.apphance.flow.plugins.android.buildplugin.AndroidPlugin
import com.apphance.flow.plugins.android.jarlibrary.AndroidJarLibraryPlugin
import com.apphance.flow.plugins.android.release.AndroidReleasePlugin
import com.apphance.flow.plugins.android.test.AndroidTestPlugin
import com.apphance.flow.plugins.ios.apphance.IOSApphancePlugin
import com.apphance.flow.plugins.ios.buildplugin.IOSPlugin
import com.apphance.flow.plugins.ios.framework.IOSFrameworkPlugin
import com.apphance.flow.plugins.ios.release.IOSReleasePlugin
import com.apphance.flow.plugins.project.ProjectPlugin
import com.apphance.flow.plugins.release.ReleasePlugin
import com.google.inject.AbstractModule
import com.google.inject.Guice
import org.gradle.api.Project
import org.gradle.api.plugins.PluginContainer
import spock.lang.Specification
import spock.lang.Unroll

import static com.apphance.flow.configuration.ProjectConfiguration.LOG_DIR
import static com.apphance.flow.configuration.reader.GradlePropertiesPersister.FLOW_PROP_FILENAME
import static com.apphance.flow.detection.project.ProjectType.ANDROID
import static com.apphance.flow.detection.project.ProjectType.IOS
import static com.apphance.flow.di.ConfigurationModule.getVariantFactories
import static com.apphance.flow.plugins.ios.parsers.XCodeOutputParserSpec.XCODE_LIST

class PluginMasterSpec extends Specification {

    @Unroll
    def 'test if all #type plugins are applied'() {
        given:
        def mocks = (plugins + commonPlugins).collect(mockToMap).sum()
        def master = createInjectorForPluginsMocks(mocks, file, type).getInstance(PluginMaster)

        and:
        def project = Mock(Project)
        project.plugins >> Mock(PluginContainer)
        def amebaProperties = Mock(File)
        amebaProperties.exists() >> true
        project.file(FLOW_PROP_FILENAME) >> amebaProperties

        and:
        projectTypeDetectorMock.detectProjectType(_) >> type

        when:
        master.enhanceProject(project)

        then:
        interaction {
            mocks.each { type, instance ->
                1 * instance.apply(project)
            }
        }

        where:
        type    | plugins        | file
        ANDROID | androidPlugins | 'AndroidManifest.xml'
        IOS     | iosPlugins     | 'GradleXCode.xcodeproj'
    }

    def 'test Android plugins order'() {
        given:
        def mocks = (commonPlugins + androidPlugins).collect(mockToMap).sum()
        def master = createInjectorForPluginsMocks(mocks, 'AndroidManifest.xml', ANDROID).getInstance(PluginMaster)

        and:
        def project = Mock(Project)
        project.plugins >> Mock(PluginContainer)
        def amebaProperties = Mock(File)
        amebaProperties.exists() >> true
        project.file(FLOW_PROP_FILENAME) >> amebaProperties

        and: 'tell that project is Android'
        projectTypeDetectorMock.detectProjectType(_) >> ANDROID

        when:
        master.enhanceProject(project)

        then:
        1 * mocks[before].apply(project)

        then:
        1 * mocks[after].apply(project)

        where:
        before        | after
        ProjectPlugin | AndroidPlugin
        AndroidPlugin | ReleasePlugin
        ReleasePlugin | AndroidReleasePlugin
    }

    def 'test iOS plugins order'() {
        given:
        def mocks = (commonPlugins + iosPlugins).collect(mockToMap).sum()
        def master = createInjectorForPluginsMocks(mocks, 'GradleXCode.xcodeproj', IOS).getInstance(PluginMaster)

        and:
        def project = Mock(Project)
        project.plugins >> Mock(PluginContainer)
        def amebaProperties = Mock(File)
        amebaProperties.exists() >> true
        project.file(FLOW_PROP_FILENAME) >> amebaProperties

        and: 'tell that project is iOS'
        projectTypeDetectorMock.detectProjectType(_) >> IOS

        when:
        master.enhanceProject(project)

        then:
        1 * mocks[before].apply(project)

        then:
        1 * mocks[after].apply(project)

        where:
        before        | after
        ProjectPlugin | IOSPlugin
        IOSPlugin     | ReleasePlugin
        ReleasePlugin | IOSReleasePlugin
    }

    final projectTypeDetectorMock = Mock(ProjectTypeDetector)

    def mockToMap = { [(it): Mock(it)] }

    static commonPlugins = [ProjectPlugin, ReleasePlugin]

    static androidPlugins = [
            AndroidPlugin,
            AndroidAnalysisPlugin,
            AndroidApphancePlugin,
            AndroidJarLibraryPlugin,
            AndroidReleasePlugin,
            AndroidTestPlugin
    ]

    static iosPlugins = [
            IOSPlugin,
            IOSFrameworkPlugin,
            IOSReleasePlugin,
            IOSApphancePlugin,
//            IOSUnitTestPlugin,//TODO restore after tests are implemented
    ]

    def createInjectorForPluginsMocks(mocks, file, projectType) {
        def rootDir = Mock(File)
        rootDir.list() >> [file]
        def project = GroovyMock(Project)

        def iosExecutorMock = GroovyStub(IOSExecutor)
        iosExecutorMock.list >> XCODE_LIST

        project.rootDir >> rootDir
        project.file(LOG_DIR) >> new File(System.properties['java.io.tmpdir'])
        project.ant >> Mock(org.gradle.api.AntBuilder)

        return Guice.createInjector(
                new EnvironmentModule(),
                new IOSModule(project),
                new AndroidModule(project),
                new CommandExecutorModule(project),
                new ConfigurationModule(project),
                new AbstractModule() {

                    @Override
                    protected void configure() {
                        bind(Project).toInstance(project)
                        bind(ProjectTypeDetector).toInstance(projectTypeDetectorMock)
                        bind(org.gradle.api.AntBuilder).toInstance(project.ant)
                        mocks.each { type, instance ->
                            bind(type).toInstance(instance)
                        }
                        if (projectType == IOS) {
                            bind(IOSExecutor).toInstance(iosExecutorMock)
                        }
                        variantFactories[projectType].each { install(it) }
                    }
                })
    }
}
