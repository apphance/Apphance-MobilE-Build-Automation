package com.apphance.ameba.android.plugins.analysis

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.PluginHelper
import com.apphance.ameba.android.AndroidProjectConfiguration
import com.apphance.ameba.android.AndroidProjectConfigurationRetriever
import com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Provides static code analysis.
 *
 */
class AndroidAnalysisPlugin implements Plugin<Project> {
    static Logger logger = Logging.getLogger(AndroidAnalysisPlugin.class)

    static final String FINDBUGS_HOME_DIR_PROPERTY = 'findbugs.home.dir'

    static final String FINDBUGS_DEFAULT_HOME = '/var/lib/analysis/findbugs'

    File findbugsHomeDir
    AndroidProjectConfiguration androidConf

    public void apply(Project project) {
        PluginHelper.checkAllPluginsAreLoaded(project, this.class, AndroidPlugin.class)
        def androidAnalysisConvention = new AndroidAnalysisConvention()
        project.convention.plugins.put('androidAnalysis', androidAnalysisConvention)
        this.androidConf = AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration(project)
        preparePmdTask(project)
        prepareFindbugsTask(project)
        prepareCpdTask(project)
        prepareCheckstyleTask(project)
        prepareAnalysisTask(project)
    }

    private void preparePmdTask(Project project) {
        def task = project.task('pmd')
        task.description = "Runs PMD analysis on project"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_ANALYSIS
        project.configurations.add('pmdConf')
        project.dependencies.add('pmdConf', 'pmd:pmd:4.2.6')
        task << {
            URL pmdXml = getResourceUrl(project, 'pmd-rules.xml')
            def analysisDir = project.file('build/analysis')
            def pmdFile = new File(analysisDir, "pmd-rules.xml")
            pmdFile.parentFile.mkdirs()
            pmdFile.delete()
            pmdFile << pmdXml.getContent()
            project.ant {
                taskdef(name: 'pmd', classname: 'net.sourceforge.pmd.ant.PMDTask',
                        classpath: project.configurations.pmdConf.asPath)
                pmd(shortFilenames: 'false', failonruleviolation: 'false',
                        rulesetfiles: "build/analysis/pmd-rules.xml") {
                    formatter(type: 'xml', toFile: "build/analysis/pmd-result.xml")
                    fileset(dir: 'src') { include(name: '**/*.java') }
                }
            }
        }
    }

    private void prepareCpdTask(Project project) {
        def task = project.task('cpd')
        task.description = "Runs CPD (duplicated code) analysis on project"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_ANALYSIS
        task << {
            project.ant {
                taskdef(name: 'cpd', classname: 'net.sourceforge.pmd.cpd.CPDTask',
                        classpath: project.configurations.pmdConf.asPath)
                cpd(minimumTokenCount: '100', format: 'xml', encoding: 'UTF-8',
                        outputFile: "build/analysis/cpd-result.xml") {
                    fileset(dir: 'src', includes: '**/*.java')
                }
            }
        }
    }

    private URL getResourceUrl(Project project, String resourceName) {
        logger.info("Reading resource ${resourceName}")
        AndroidAnalysisConvention convention = project.convention.plugins.androidAnalysis
        URL configUrl = project.file('config/analysis').toURI().toURL()
        URL baseUrl = configUrl
        if (convention.baseAnalysisConfigUrl != null) {
            baseUrl = new URL(convention.baseAnalysisConfigUrl)
            logger.info("Base config url  ${baseUrl}")
        }
        URL targetURL = new URL(baseUrl, resourceName)
        if (targetURL.getProtocol() != 'file') {
            logger.info("Downloading file from ${targetURL}")
            try {
                targetURL.getContent() // just checking if we can read it
                return targetURL
            } catch (IOException e) {
                logger.warn("Exception ${e} while reading from ${targetURL}. Falling back")
                targetURL = new URL(configUrl, resourceName)
            }
        }
        logger.info("Reading resource from file ${targetURL}")
        if (!(new File(targetURL.toURI()).exists())) {
            def url = this.class.getResource(resourceName)
            logger.info("Reading resource from internal ${url} as file ${targetURL} not found")
            return url
        }
        return targetURL
    }

    private void prepareFindbugsTask(Project project) {
        def task = project.task('findbugs')
        task.description = "Runs Findbugs analysis on project"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_ANALYSIS
        project.configurations.add('findbugsConf')
        project.dependencies.add('findbugsConf', 'com.google.code.findbugs:findbugs:2.0.0')
        project.dependencies.add('findbugsConf', 'com.google.code.findbugs:findbugs-ant:2.0.0')
        task << {
            URL findbugsXml = getResourceUrl(project, 'findbugs-exclude.xml')
            def analysisDir = project.file('build/analysis')
            def findbugsFile = new File(analysisDir, "findbugs-exclude.xml")
            findbugsFile.parentFile.mkdirs()
            findbugsFile.delete()
            findbugsFile << findbugsXml.getContent()
            if (project.hasProperty(FINDBUGS_HOME_DIR_PROPERTY)) {
                this.findbugsHomeDir = new File(project[FINDBUGS_HOME_DIR_PROPERTY])
            } else {
                this.findbugsHomeDir = new File(FINDBUGS_DEFAULT_HOME)
            }
            if (!this.findbugsHomeDir.exists() && !this.findbugsHomeDir.isDirectory()) {
                throw new GradleException("The file ${this.findbugsHomeDir} should point to findbugs home directory. You can change it by specifying -Pfindbugs.home.dir=<DIR>")
            }
            project.ant {
                taskdef(name: 'findbugs', classname: 'edu.umd.cs.findbugs.anttask.FindBugsTask',
                        classpath: project.configurations.findbugsConf.asPath)
                findbugs(home: this.findbugsHomeDir.absolutePath,
                        output: 'xml', outputFile: "build/analysis/findbugs-result.xml",
                        excludefilter: "build/analysis/findbugs-exclude.xml") {
                    sourcePath(path: 'src')
                    "class"(location: project.file('bin/classes'))
                    auxclassPath(path: androidConf.allJarsAsPath)
                }
            }
        }
        task.dependsOn(project.classes)
    }

    private void prepareCheckstyleTask(Project project) {
        def task = project.task('checkstyle')
        task.description = "Runs Checkstyle analysis on project"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_ANALYSIS
        project.configurations.add('checkstyleConf')
        project.dependencies.add('checkstyleConf', 'checkstyle:checkstyle:5.0')
        task << {
            URL checkstyleXml = getResourceUrl(project, 'checkstyle.xml')
            def analysisDir = project.file('build/analysis')
            def checkstyleFile = new File(analysisDir, "checkstyle.xml")
            checkstyleFile.parentFile.mkdirs()
            checkstyleFile.delete()
            checkstyleFile << checkstyleXml.getContent()
            URL checkstyleSuppressionsXml = getResourceUrl(project, 'checkstyle-suppressions.xml')
            def checkstyleSuppressionsFile = new File(analysisDir, "checkstyle-suppressions.xml")
            checkstyleSuppressionsFile.parentFile.mkdirs()
            checkstyleSuppressionsFile.delete()
            checkstyleSuppressionsFile << checkstyleSuppressionsXml.getContent()
            URL checkstyleLocalSuppressionsXml = getResourceUrl(project, 'checkstyle-local-suppressions.xml')
            def checkstyleLocalSuppressionsFile = new File(analysisDir, "checkstyle-local-suppressions.xml")
            checkstyleLocalSuppressionsFile.parentFile.mkdirs()
            checkstyleLocalSuppressionsFile.delete()
            checkstyleLocalSuppressionsFile << checkstyleLocalSuppressionsXml.getContent()
            project.ant {
                taskdef(resource: 'checkstyletask.properties',
                        classpath: project.configurations.checkstyleConf.asPath)
                checkstyle(config: "build/analysis/checkstyle.xml",
                        failOnViolation: false) {
                    formatter(type: 'xml', tofile: "build/analysis/checkstyle-report.xml")
                    classpath(path: 'bin/classes')
                    classpath(path: project.configurations.checkstyleConf.asPath)
                    classpath(path: androidConf.allJarsAsPath)
                    fileset(dir: 'src', includes: '**/*.java')
                }
            }
        }
        task.dependsOn(project.classes)
    }

    private void prepareAnalysisTask(Project project) {
        def task = project.task('analysis')
        task.description = "Runs all analysis on project"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_ANALYSIS
        task << {
            // do nothing - just umbrella task
        }
        task.dependsOn(project.findbugs)
        task.dependsOn(project.pmd)
        task.dependsOn(project.cpd)
        task.dependsOn(project.checkstyle)
    }

    static class AndroidAnalysisConvention {
        static public final String DESCRIPTION =
            """The convention provides base URL where analysis configuration files are placed.
The configuration files can be either internal (no configuration needed)
or taken from local configuration directory (config/analysis)
or retrieved using base URL specified."""
        def String baseAnalysisConfigUrl = null

        def androidAnalysis(Closure close) {
            close.delegate = this
            close.run()
        }
    }

    static public final String DESCRIPTION =
        """This plugin provides capability of running basic static code analysis on android.

It provides analysis task, that executes checkstyle, findbugs, pmd tasks (soon also lint).

Note that the findbugs plugin requires findbugs to be installed and it's home has to be configured.
By default the home of findbugs is in '${FINDBUGS_DEFAULT_HOME}' but it can be configured
by specifying gradle's property: ${FINDBUGS_HOME_DIR_PROPERTY}. Currently supported findbugs version is 2.0.0 and
this version has to be installed.

The Easiest way it is to add it in gradle.properties or specified in organisation-specific conventions:
<code>
this['${FINDBUGS_HOME_DIR_PROPERTY}'] = 'SPECIFY HOME DIRECTORY'
</code>

Configuration files for all analysis taks are retrieved from internal configuration.
The configuration files can also be present in local config directory in the project (for project-specific configuration)
They can also be downloaded remotely using http if specified in convention - this is for organisation-wide setup for many projects.
The convention or local config files (if present) take precedence over the internal configuration, however if specific
configuration file cannot be found, the internal version is used.

The structure of the directory/URL is as follows:
<code>
[config/analysis] directory or base URL
+---+- checkstyle.xml : main checkstyle configuration
    +- checkstyle-suppressions.xml : suppressions from checkstyle
    +- checkstyle-local-suppressions.xml : additional local suppressions
    +- findbugs-exclude.xml : findbugs excludes configuration
    +- pmd-rules.xml : pmd rules
</code>
Example of configuration files that can be used as starting point for your local/project configuration
can be found
<a href="https://github.com/apphance/Apphance-MobilE-Build-Automation/tree/master/src/main/resources/com/apphance/ameba/android/plugins/analysis">here</a>
"""
}
