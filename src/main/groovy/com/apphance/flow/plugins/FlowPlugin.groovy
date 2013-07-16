package com.apphance.flow.plugins

import com.apphance.flow.di.*
import com.apphance.flow.util.Version
import com.google.inject.Guice
import groovy.transform.PackageScope
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

import static org.gradle.api.logging.Logging.getLogger

class FlowPlugin implements Plugin<Project> {

    static final Version BORDER_VERSION = new Version('1.7')

    def l = getLogger(getClass())


    @Override
    void apply(Project project) {
        l.lifecycle(FLOW_ASCII_ART)

        validateJavaRuntimeVersion()

        def injector = Guice.createInjector(
                new GradleModule(project),
                new IOSModule(project),
                new AndroidModule(project),
                new ConfigurationModule(project),
                new EnvironmentModule(),
                new CommandExecutorModule(project),
        )
        injector.getInstance(PluginMaster).enhanceProject(project)

        project.tasks.each { injector.injectMembers(it) }
    }

    @PackageScope
    void validateJavaRuntimeVersion() {
        def runtimeVersion = new Version(trimmedJavaVersion)
        if (runtimeVersion.compareTo(BORDER_VERSION) == -1)
            throw new GradleException("Invalid JRE version: $runtimeVersion.version! " +
                    "Minimal JRE version is: $BORDER_VERSION.version")
    }

    @PackageScope
    String getTrimmedJavaVersion() {
        String version = System.properties['java.version']
        Integer underscore = version.indexOf('_')
        version.substring(0, underscore >= 0 ? underscore : version.length()).trim()
    }

    static String FLOW_ASCII_ART = """
        |    _             _                       ___ _
        |   /_\\  _ __ _ __| |_  __ _ _ _  __ ___  | __| |_____ __ __
        |  / _ \\| '_ \\ '_ \\ ' \\/ _` | ' \\/ _/ -_) | _|| / _ \\ V  V /
        | /_/ \\_\\ .__/ .__/_||_\\__,_|_||_\\__\\___| |_| |_\\___/\\_/\\_/
        |       |_|  |_|
        |
        |""".stripMargin()
}
