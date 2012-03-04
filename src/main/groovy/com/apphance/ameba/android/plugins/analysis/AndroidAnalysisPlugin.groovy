package com.apphance.ameba.android.plugins.analysis

import groovy.util.XmlSlurper

import java.io.File

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectHelper;
import com.apphance.ameba.android.AndroidProjectConfiguration
import com.apphance.ameba.android.AndroidProjectConfigurationRetriever
import com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin;

class AndroidAnalysisPlugin implements Plugin<Project>{

    File findbugsHomeDir
    AndroidProjectConfigurationRetriever androidConfRetriever
    AndroidProjectConfiguration androidConf

    static public final String DESCRIPTION ="""
    <div>
    <div>This plugin provides capabulity of running basic static code analysis on android.</div>
    <div><br></div>
    <div>It provides analysis task, that executes checkstyle, findbugs, pmd tasks (soon also lint).
Not that the findbugs plugin requires findbugs to be installed. </div>
    </div>
    """


    public void apply(Project project) {
        ProjectHelper.checkAllPluginsAreLoaded(project, this.class, AndroidPlugin.class)
        def androidAnalysisConvention = new AndroidAnalysisConvention()
        project.convention.plugins.put('androidAnalysis', androidAnalysisConvention)
        this.androidConfRetriever = new AndroidProjectConfigurationRetriever()
        this.androidConf = androidConfRetriever.getAndroidProjectConfiguration(project)
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
        project.dependencies.add('pmdConf', 'pmd:pmd:4.2.6' )
        task << {
            URL pmdXml = this.class.getResource('pmd-rules.xml')
            def analysisDir = new File(project.rootDir,project.androidAnalysis.buildAnalysisDirectory)
            def pmdFile = new File(analysisDir,"pmd-rules.xml")
            pmdFile.parentFile.mkdirs()
            pmdFile.delete()
            pmdFile << pmdXml.getContent()
            project.ant {
                taskdef(name:'pmd', classname:'net.sourceforge.pmd.ant.PMDTask',
                        classpath: project.configurations.pmdConf.asPath)
                pmd(shortFilenames:'false', failonruleviolation:'false',
                        rulesetfiles:"${project.androidAnalysis.buildAnalysisDirectory}/pmd-rules.xml") {
                            formatter(type:'xml', toFile:"${project.androidAnalysis.buildAnalysisDirectory}/pmd-result.xml")
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
                taskdef(name:'cpd', classname:'net.sourceforge.pmd.cpd.CPDTask',
                        classpath: project.configurations.pmdConf.asPath)
                cpd(minimumTokenCount:'100', format: 'xml', encoding: 'UTF-8',
                        outputFile:"${project.androidAnalysis.buildAnalysisDirectory}/cpd-result.xml") {
                            fileset(dir: 'src', includes: '**/*.java')
                        }
            }
        }
    }

    private void prepareFindbugsTask(Project project) {
        def task = project.task('findbugs')
        task.description = "Runs Findbugs analysis on project"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_ANALYSIS
        project.configurations.add('findbugsConf')
        project.dependencies.add('findbugsConf', 'com.google.code.findbugs:findbugs:1.3.9' )
        project.dependencies.add('findbugsConf', 'com.google.code.findbugs:findbugs-ant:1.3.9' )
        task << {
            URL findbugsXml = this.class.getResource('findbugs-exclude.xml')
            def analysisDir = new File(project.rootDir,project.androidAnalysis.buildAnalysisDirectory)
            def findbugsFile = new File(analysisDir,"findbugs-exclude.xml")
            findbugsFile.parentFile.mkdirs()
            findbugsFile.delete()
            findbugsFile << findbugsXml.getContent()
            if (project.hasProperty('findbugs.home.dir')) {
                findbugsHomeDir = new File(project['findbugs.home.dir'])
            } else {
                findbugsHomeDir = new File('/var/lib/analysis/findbugs')
            }
            if (!findbugsHomeDir.exists()  && !findbugsHomeDir.isDirectory()) {
                throw new GradleException("The file ${findbugsHomeDir} should point to findbugs home directory. You can change it by specifying -Pfindbugs.home.dir=<DIR>")
            }
            project.ant {
                taskdef(name:'findbugs', classname:'edu.umd.cs.findbugs.anttask.FindBugsTask',
                        classpath: project.configurations.findbugsConf.asPath)
                findbugs(home: findbugsHomeDir.absolutePath,
                        output:'xml', outputFile:"${project.androidAnalysis.buildAnalysisDirectory}/findbugs-result.xml",
                        excludefilter:"${project.androidAnalysis.buildAnalysisDirectory}/findbugs-exclude.xml") {
                            sourcePath(path: 'src')
                            "class"(location:'bin/classes')
                            auxclassPath(path: androidConf.allJarsAsPath)
                        }
            }
        }
        task.dependsOn(project.classes)
    }

    private String replacePathsInCheckStyle(Project project, Object content) {
        byte[] contents = new byte[5 * 1024];

        int bytesRead=0;
        String strFileContents;

        while( (bytesRead = content.read(contents)) != -1){
            strFileContents = new String(contents, 0, bytesRead);
        }
        def xmlSlurper = new XmlSlurper()
        xmlSlurper.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        def modules = xmlSlurper.parseText(strFileContents)
        def suppressionFilters = modules.module.findAll {it.@name.text().contains("SuppressionFilter")}
        def configFilter = suppressionFilters.findAll{it.property.@value.text().contains('config/analysis')}
        configFilter.each {it -> it.property.@value = it.property.@value.text().replaceFirst('config/analysis',
            project.androidAnalysis.configAnalysisDirectory)}
        def buildFilter = suppressionFilters.findAll{it.property.@value.text().contains('build/analysis')}
        buildFilter.each {it -> it.property.@value = it.property.@value.text().replaceFirst('build/analysis',
            project.androidAnalysis.buildAnalysisDirectory)}
        def outputBuilder = new groovy.xml.StreamingMarkupBuilder()
        outputBuilder.encoding = 'UTF-8'
        String result = outputBuilder.bind{
            mkp.xmlDeclaration()
            mkp.yieldUnescaped '<!DOCTYPE module PUBLIC "-//Puppy Crawl//DTD Check Configuration 1.3//EN" "http://www.puppycrawl.com/dtds/configuration_1_3.dtd">'
            mkp.yield modules
        }
        return result
    }

    private void prepareCheckstyleTask(Project project) {
        def task = project.task('checkstyle')
        task.description = "Runs Checkstyle analysis on project"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_ANALYSIS
        project.configurations.add('checkstyleConf')
        project.dependencies.add('checkstyleConf','checkstyle:checkstyle:5.0')
        task << {
            URL checkstyleXml = this.class.getResource('checkstyle.xml')
            def analysisDir = new File(project.rootDir,project.androidAnalysis.buildAnalysisDirectory)
            def checkstyleFile = new File(analysisDir,"checkstyle.xml")
            checkstyleFile.parentFile.mkdirs()
            checkstyleFile.delete()
            checkstyleFile << replacePathsInCheckStyle(project, checkstyleXml.getContent())
            URL checkstyleSuppressionsXml = this.class.getResource('checkstyle-suppressions.xml')
            def checkstyleSuppressionsFile = new File(analysisDir,"checkstyle-suppressions.xml")
            checkstyleSuppressionsFile.delete()
            checkstyleSuppressionsFile << checkstyleSuppressionsXml.getContent()
            URL checkstyleLocalSuppressionsXml = this.class.getResource('checkstyle-local-suppressions.xml')
            def configAnalysisDir = new File(project.rootDir, project.androidAnalysis.configAnalysisDirectory)
            def checkstyleLocalSuppressionsFile = new File(configAnalysisDir,"checkstyle-local-suppressions.xml")
            checkstyleLocalSuppressionsFile.parentFile.mkdirs()
            if (!checkstyleLocalSuppressionsFile.exists()) {
                checkstyleLocalSuppressionsFile << checkstyleLocalSuppressionsXml.getContent()
            }
            project.ant {
                taskdef(resource:'checkstyletask.properties',
                        classpath: project.configurations.checkstyleConf.asPath)
                checkstyle(config: "${project.androidAnalysis.buildAnalysisDirectory}/checkstyle.xml",
                        failOnViolation: false) {
                            formatter(type: 'xml', tofile:"${project.androidAnalysis.buildAnalysisDirectory}/checkstyle-report.xml")
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
        static public final String DESCRIPTION ="The conventions provide places where analysis configuration files are placed."
        def String buildAnalysisDirectory = 'build/analysis'
        def String configAnalysisDirectory = 'config/analysis'

        def androidAnalysis(Closure close) {
            close.delegate = this
            close.run()
        }
    }
}
