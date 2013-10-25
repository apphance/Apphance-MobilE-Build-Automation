package com.apphance.flow.plugins

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.di.*
import com.apphance.flow.util.Version
import com.apphance.flow.validation.ConfigurationValidator
import com.google.inject.Guice
import groovy.transform.PackageScope
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.flow.configuration.reader.GradlePropertiesPersister.FLOW_PROP_FILENAME
import static org.gradle.api.logging.Logging.getLogger

class FlowPlugin implements Plugin<Project> {

    private final static Version BORDER_VERSION = new Version('1.7')
    private logger = getLogger(getClass())
    @Inject Map<Integer, AbstractConfiguration> configurations
    @Inject ConfigurationValidator configurationValidator

    @Override
    void apply(Project project) {
        logger.lifecycle FLOW_ASCII_ART

        String version = flowVersion(project)
        logger.lifecycle "Apphance Flow version: ${version}\n"

        validateJavaRuntimeVersion()

        def injector = Guice.createInjector(
                new GradleModule(project),
                new IOSModule(project),
                new AndroidModule(project),
                new ConfigurationModule(project),
                new EnvironmentModule(),
                new CommandExecutorModule(project),
        )

        injector.injectMembers(this)
        validateConfiguration(project.file(FLOW_PROP_FILENAME))

        injector.getInstance(PluginMaster).enhanceProject(project)

        project.tasks.each { injector.injectMembers(it) }
    }

    String flowVersion(Project project) {
        File flowJar = project.buildscript.configurations.classpath.find { it.name.contains('apphance-flow') } as File
        extractVersionFromFilename(flowJar?.name)
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

    @PackageScope
    String extractVersionFromFilename(String fileName) {
        def regex = /.*-(\d.*).jar/
        def matcher = (fileName =~ regex)
        matcher.matches() ? (matcher[0])?.get(1) : ''
    }

    @PackageScope
    void validateConfiguration(File properties) {
        if (properties?.exists() && properties?.isFile() && properties?.size() > 0) {
            logger.info("Running configuration validation")
            configurationValidator.validate(configurations.values())
        } else {
            logger.debug("Skipping configuration validation - ${properties?.absolutePath} does not exist")
        }
    }

    static String FLOW_ASCII_ART = """
        |    _             _                       ___ _
        |   /_\\  _ __ _ __| |_  __ _ _ _  __ ___  | __| |_____ __ __
        |  / _ \\| '_ \\ '_ \\ ' \\/ _` | ' \\/ _/ -_) | _|| / _ \\ V  V /
        | /_/ \\_\\ .__/ .__/_||_\\__,_|_||_\\__\\___| |_| |_\\___/\\_/\\_/
        |       |_|  |_|
        |""".stripMargin()
}
