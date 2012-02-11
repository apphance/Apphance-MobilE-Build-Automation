package com.apphance.ameba.android.plugins.jarlibrary



import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.android.AndroidManifestHelper
import com.apphance.ameba.android.AndroidProjectConfiguration;
import com.apphance.ameba.android.AndroidProjectConfigurationRetriever;

/**
 * Helps building the library with resources embedded. It is useful in case we want to generate libraries like
 * apphance.jar that want to have the resources embedded (in res-raw)
 */
class AndroidJarLibraryPlugin implements Plugin<Project>{

    static Logger logger = Logging.getLogger(AndroidJarLibraryPlugin.class)

    ProjectHelper projectHelper
    ProjectConfiguration conf
    AndroidManifestHelper manifestHelper
    AndroidProjectConfigurationRetriever androidConfRetriever
    AndroidProjectConfiguration androidConf
    String jarLibraryPrefix

    public void apply(Project project) {
        this.projectHelper = new ProjectHelper()
        this.conf = this.projectHelper.getProjectConfiguration(project)
        this.androidConfRetriever = new AndroidProjectConfigurationRetriever()
        this.androidConf = androidConfRetriever.getAndroidProjectConfiguration(project)
        manifestHelper = new AndroidManifestHelper()
        if (project.hasProperty('android.jarLibrary.resPrefix')) {
            jarLibraryPrefix = project['android.jarLibrary.resPrefix']
        } else {
            jarLibraryPrefix = this.androidConf.mainProjectName
        }
        prepareJarLibraryTask(project)
    }

    public void prepareJarLibraryTask(Project project) {
        def task = project.task('jarLibrary')
        task.description = "Prepares jar library with embedded resources"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
        task << {
            conf.tmpDirectory.mkdirs()
            def manifestFile = new File(conf.tmpDirectory, 'MANIFEST.MF')
            project.ant.manifest (file: manifestFile) {
                attribute(name : 'Specification-Title', value: androidConf.mainProjectName)
                attribute(name : 'Specification-Vendor', value: androidConf.mainProjectName)
                attribute(name : 'Implementation-Title', value: conf.versionString)
                attribute(name : 'Implementation-Version', value: conf.versionCode)
                attribute(name : 'Implementation-Vendor', value: androidConf.mainProjectName)
                attribute(name : 'Implementation-Vendor-Id', value: androidConf.mainProjectName)
            }
            def manifestPropertiesFile = new File(conf.tmpDirectory,'manifest.properties')
            def properties = new Properties()
            properties.setProperty("implementation.title", conf.versionString)
            properties.setProperty("implementation.version", Integer.toString(conf.versionCode))
            properties.store(manifestPropertiesFile.newOutputStream(), "Automatically generated with Ameba")
            File resDir = new File(conf.tmpDirectory, "${jarLibraryPrefix}-res")
            project.ant.delete(dir : resDir)
            resDir.mkdirs()
            project.ant.copy(todir: resDir) {
                fileset(dir: new File(project.rootDir, 'res'))
            }
            File destFile = new File(project.rootDir,"bin/${androidConf.mainProjectName}_${conf.versionString}.jar")
            File classesDir = new File(project.rootDir, "bin/classes")
            destFile.delete()
            project.ant.jar(destfile: destFile, manifest: manifestFile, manifestencoding: 'utf-8') {
                fileset(dir: classesDir) {
                    include(name: '**/*.class')
                    exclude(name: '**/test/*.class')
                    exclude(name: 'R*.class')
                }
                fileset(dir: conf.tmpDirectory) {
                    include(name : 'manifest.properties')
                    include(name: "${resDir.name}/**")
                    exclude(name: '**/test*.*')
                    exclude(name: "${resDir.name}/raw/config.properties")
                }
            }
        }
        task.dependsOn(project.readAndroidProjectConfiguration)
    }
}
